package backend;

import backend.llvm.*;
import frontend.Token;
import frontend.ast.*;
import frontend.ast.exp.*;
import frontend.ast.stmt.*;
import frontend.ast.terminal.*;
import midend.symbol.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class LLVMIRGenerator {
    private CompUnitNode astRoot;
    private SymbolTable symbolTable;
    private IRModule module;
    
    private int regCounter = 0;
    private int labelCounter = 0;
    private int stringCounter = 0;
    
    private IRFunction currentFunction;
    private IRBasicBlock currentBlock;
    
    // Maps variable names to their IR values (pointers for alloca)
    private HashMap<String, IRValue> varMap = new HashMap<>();
    
    // Stack for managing scopes
    private Stack<HashMap<String, IRValue>> scopeStack = new Stack<>();
    
    // For loops: track break/continue labels
    private Stack<String> breakLabels = new Stack<>();
    private Stack<String> continueLabels = new Stack<>();
    
    public LLVMIRGenerator(CompUnitNode astRoot, SymbolTable symbolTable) {
        this.astRoot = astRoot;
        this.symbolTable = symbolTable;
        this.module = new IRModule();
    }
    
    public String generate() {
        visitCompUnit(astRoot);
        return module.toString();
    }
    
    private String newRegister() {
        return "%" + (regCounter++);
    }
    
    private String newLabel(String prefix) {
        return prefix + (labelCounter++);
    }
    
    private String newStringLabel() {
        return "@.str." + (stringCounter++);
    }
    
    private void enterScope() {
        scopeStack.push(new HashMap<>(varMap));
    }
    
    private void exitScope() {
        if (!scopeStack.isEmpty()) {
            varMap = scopeStack.pop();
        }
    }
    
    private void visitCompUnit(CompUnitNode node) {
        // Process global declarations
        for (DeclNode decl : node.getDecls()) {
            visitGlobalDecl(decl);
        }
        
        // Process function definitions
        for (FuncDefNode funcDef : node.getFuncDefs()) {
            visitFuncDef(funcDef);
        }
        
        // Process main function
        MainFuncDefNode mainFunc = node.getMainFuncDef();
        if (mainFunc != null) {
            visitMainFuncDef(mainFunc);
        }
    }
    
    private void visitGlobalDecl(DeclNode node) {
        // Global declarations - handle VarDecl and ConstDecl
        for (ASTnode child : node.children) {
            if (child instanceof VarDeclNode) {
                visitGlobalVarDecl((VarDeclNode) child);
            } else if (child instanceof ConstDeclNode) {
                visitGlobalConstDecl((ConstDeclNode) child);
            }
        }
    }
    
    private void visitGlobalVarDecl(VarDeclNode node) {
        for (ASTnode child : node.children) {
            if (child instanceof VarDefNode) {
                VarDefNode varDef = (VarDefNode) child;
                IdentNode identNode = (IdentNode) varDef.children.get(0);
                String varName = identNode.token.getValue();
                
                // Check if it's an array
                boolean isArray = varDef.children.stream()
                    .anyMatch(n -> n instanceof TokenNode && 
                             ((TokenNode)n).token.getTokenType() == Token.TokenType.LBRACK);
                
                if (isArray) {
                    // TODO: Handle global arrays
                } else {
                    IRValue globalVar = new IRValue(IRType.getPointerType(IRType.I32), "@" + varName);
                    module.addGlobalVariable("@" + varName, new IRValue(0));
                    varMap.put(varName, globalVar);
                }
            }
        }
    }
    
    private void visitGlobalConstDecl(ConstDeclNode node) {
        for (ASTnode child : node.children) {
            if (child instanceof ConstDefNode) {
                ConstDefNode constDef = (ConstDefNode) child;
                IdentNode identNode = (IdentNode) constDef.children.get(0);
                String varName = identNode.token.getValue();
                
                // For const, we can try to evaluate the value
                // For simplicity, treat as global variable
                IRValue globalVar = new IRValue(IRType.getPointerType(IRType.I32), "@" + varName);
                module.addGlobalVariable("@" + varName, new IRValue(0));
                varMap.put(varName, globalVar);
            }
        }
    }
    
    private void visitFuncDef(FuncDefNode node) {
        FuncTypeNode funcType = (FuncTypeNode) node.children.get(0);
        boolean isVoid = funcType.getTokenValue().equals("void");
        IRType returnType = isVoid ? IRType.VOID : IRType.I32;
        
        IdentNode identNode = (IdentNode) node.children.get(1);
        String funcName = identNode.token.getValue();
        
        currentFunction = new IRFunction(funcName, returnType);
        regCounter = 1; // Reset register counter for each function
        
        // Create entry block
        currentBlock = new IRBasicBlock("entry");
        currentFunction.addBasicBlock(currentBlock);
        
        // Enter function scope
        enterScope();
        
        // Handle parameters
        if (node.children.get(3) instanceof FuncFParamsNode) {
            FuncFParamsNode paramsNode = (FuncFParamsNode) node.children.get(3);
            visitFuncParams(paramsNode);
        }
        
        // Visit function body
        BlockNode blockNode = (BlockNode) node.children.get(node.children.size() - 1);
        visitFunctionBlock(blockNode);
        
        // Ensure function has a return
        if (returnType.isVoid()) {
            currentBlock.addInstruction(new RetInst());
        }
        
        exitScope();
        module.addFunction(currentFunction);
        currentFunction = null;
    }
    
    private void visitFuncParams(FuncFParamsNode node) {
        // First pass: count parameters and reserve registers
        int paramCount = 0;
        for (ASTnode child : node.children) {
            if (child instanceof FuncFParamNode) {
                paramCount++;
            }
        }
        
        // Reserve registers for parameters
        for (int i = 0; i < paramCount; i++) {
            newRegister();
        }
        
        // Second pass: process parameters
        int paramIndex = 1;
        for (ASTnode child : node.children) {
            if (child instanceof FuncFParamNode) {
                FuncFParamNode paramNode = (FuncFParamNode) child;
                IdentNode identNode = (IdentNode) paramNode.children.get(1);
                String paramName = identNode.token.getValue();
                
                // Use pre-allocated parameter register
                String paramReg = "%" + paramIndex;
                IRValue paramValue = new IRValue(IRType.I32, paramReg);
                currentFunction.addParam(paramValue);
                
                // Allocate space for parameter and store it
                String allocaReg = newRegister();
                IRValue allocaPtr = new IRValue(IRType.getPointerType(IRType.I32), allocaReg);
                currentBlock.addInstruction(new AllocaInst(allocaPtr, IRType.I32));
                currentBlock.addInstruction(new StoreInst(paramValue, allocaPtr));
                
                varMap.put(paramName, allocaPtr);
                paramIndex++;
            }
        }
    }
    
    private void visitMainFuncDef(MainFuncDefNode node) {
        currentFunction = new IRFunction("main", IRType.I32);
        regCounter = 1;
        
        currentBlock = new IRBasicBlock("entry");
        currentFunction.addBasicBlock(currentBlock);
        
        enterScope();
        
        BlockNode blockNode = (BlockNode) node.children.get(4);
        visitFunctionBlock(blockNode);
        
        // Only add return if block doesn't already end with one
        if (!blockEndsWithTerminator()) {
            currentBlock.addInstruction(new RetInst(IRType.I32, new IRValue(0)));
        }
        
        exitScope();
        module.addFunction(currentFunction);
        currentFunction = null;
    }
    
    private boolean blockEndsWithTerminator() {
        if (currentBlock == null) return false;
        ArrayList<IRInstruction> instructions = currentBlock.getInstructions();
        if (instructions.isEmpty()) return false;
        IRInstruction last = instructions.get(instructions.size() - 1);
        return (last instanceof RetInst || last instanceof BranchInst);
    }
    
    private void visitFunctionBlock(BlockNode node) {
        // Don't create new scope - already in function scope
        for (int i = 1; i < node.children.size() - 1; i++) {
            ASTnode child = node.children.get(i);
            if (child instanceof BlockItemNode) {
                visitBlockItem((BlockItemNode) child);
            }
        }
    }
    
    private void visitBlock(BlockNode node) {
        enterScope();
        for (int i = 1; i < node.children.size() - 1; i++) {
            ASTnode child = node.children.get(i);
            if (child instanceof BlockItemNode) {
                visitBlockItem((BlockItemNode) child);
            }
        }
        exitScope();
    }
    
    private void visitBlockItem(BlockItemNode node) {
        if (node.children.isEmpty()) return;
        
        ASTnode child = node.children.get(0);
        if (child instanceof DeclNode) {
            visitLocalDecl((DeclNode) child);
        } else if (child instanceof STMTnode) {
            visitStmt((STMTnode) child);
        }
    }
    
    private void visitLocalDecl(DeclNode node) {
        for (ASTnode child : node.children) {
            if (child instanceof VarDeclNode) {
                visitLocalVarDecl((VarDeclNode) child);
            } else if (child instanceof ConstDeclNode) {
                visitLocalConstDecl((ConstDeclNode) child);
            }
        }
    }
    
    private void visitLocalVarDecl(VarDeclNode node) {
        for (ASTnode child : node.children) {
            if (child instanceof VarDefNode) {
                VarDefNode varDef = (VarDefNode) child;
                IdentNode identNode = (IdentNode) varDef.children.get(0);
                String varName = identNode.token.getValue();
                
                // Allocate on stack
                String allocaReg = newRegister();
                IRValue allocaPtr = new IRValue(IRType.getPointerType(IRType.I32), allocaReg);
                currentBlock.addInstruction(new AllocaInst(allocaPtr, IRType.I32));
                varMap.put(varName, allocaPtr);
                
                // Check for initialization
                for (ASTnode defChild : varDef.children) {
                    if (defChild instanceof InitValNode) {
                        visitInitVal((InitValNode) defChild, allocaPtr);
                    }
                }
            }
        }
    }
    
    private void visitLocalConstDecl(ConstDeclNode node) {
        for (ASTnode child : node.children) {
            if (child instanceof ConstDefNode) {
                ConstDefNode constDef = (ConstDefNode) child;
                IdentNode identNode = (IdentNode) constDef.children.get(0);
                String varName = identNode.token.getValue();
                
                // Allocate on stack for const too
                String allocaReg = newRegister();
                IRValue allocaPtr = new IRValue(IRType.getPointerType(IRType.I32), allocaReg);
                currentBlock.addInstruction(new AllocaInst(allocaPtr, IRType.I32));
                varMap.put(varName, allocaPtr);
                
                // Initialize
                for (ASTnode defChild : constDef.children) {
                    if (defChild instanceof ConstInitValNode) {
                        visitConstInitVal((ConstInitValNode) defChild, allocaPtr);
                    }
                }
            }
        }
    }
    
    private void visitInitVal(InitValNode node, IRValue targetPtr) {
        if (node.children.isEmpty()) return;
        
        ASTnode child = node.children.get(0);
        if (child instanceof EXPnode) {
            IRValue value = visitExp((EXPnode) child);
            currentBlock.addInstruction(new StoreInst(value, targetPtr));
        }
    }
    
    private void visitConstInitVal(ConstInitValNode node, IRValue targetPtr) {
        if (node.children.isEmpty()) return;
        
        ASTnode child = node.children.get(0);
        if (child instanceof ConstExpNode) {
            IRValue value = visitConstExp((ConstExpNode) child);
            currentBlock.addInstruction(new StoreInst(value, targetPtr));
        }
    }
    
    private void visitStmt(STMTnode node) {
        if (node.children.isEmpty()) return;
        
        ASTnode stmt = node.children.get(0);
        
        if (stmt instanceof AssignStmtNode) {
            visitAssignStmt((AssignStmtNode) stmt);
        } else if (stmt instanceof ExpStmtNode) {
            visitExpStmt((ExpStmtNode) stmt);
        } else if (stmt instanceof BlockNode) {
            visitBlock((BlockNode) stmt);
        } else if (stmt instanceof IfStmtNode) {
            visitIfStmt((IfStmtNode) stmt);
        } else if (stmt instanceof ForLoopNode) {
            visitForLoop((ForLoopNode) stmt);
        } else if (stmt instanceof BreakStmtNode) {
            visitBreakStmt((BreakStmtNode) stmt);
        } else if (stmt instanceof ContinueStmtNode) {
            visitContinueStmt((ContinueStmtNode) stmt);
        } else if (stmt instanceof ReturnStmtNode) {
            visitReturnStmt((ReturnStmtNode) stmt);
        } else if (stmt instanceof PrintfStmtNode) {
            visitPrintfStmt((PrintfStmtNode) stmt);
        }
    }
    
    private void visitAssignStmt(AssignStmtNode node) {
        LValNode lval = (LValNode) node.children.get(0);
        EXPnode exp = (EXPnode) node.children.get(2);
        
        IRValue value = visitExp(exp);
        IRValue ptr = visitLValAsPointer(lval);
        currentBlock.addInstruction(new StoreInst(value, ptr));
    }
    
    private void visitExpStmt(ExpStmtNode node) {
        for (ASTnode child : node.children) {
            if (child instanceof EXPnode) {
                visitExp((EXPnode) child);
            }
        }
    }
    
    private void visitIfStmt(IfStmtNode node) {
        // if '(' Cond ')' Stmt [ 'else' Stmt ]
        CondNode cond = null;
        STMTnode thenStmt = null;
        STMTnode elseStmt = null;
        
        for (ASTnode child : node.children) {
            if (child instanceof CondNode) {
                cond = (CondNode) child;
            } else if (child instanceof STMTnode) {
                if (thenStmt == null) {
                    thenStmt = (STMTnode) child;
                } else {
                    elseStmt = (STMTnode) child;
                }
            }
        }
        
        String thenLabel = newLabel("if_then");
        String elseLabel = newLabel("if_else");
        String endLabel = newLabel("if_end");
        
        // Evaluate condition
        IRValue condValue = visitCond(cond, thenLabel, elseStmt != null ? elseLabel : endLabel);
        
        // Then block
        currentBlock = new IRBasicBlock(thenLabel);
        currentFunction.addBasicBlock(currentBlock);
        if (thenStmt != null) {
            visitStmt(thenStmt);
        }
        currentBlock.addInstruction(new BranchInst(endLabel));
        
        // Else block
        if (elseStmt != null) {
            currentBlock = new IRBasicBlock(elseLabel);
            currentFunction.addBasicBlock(currentBlock);
            visitStmt(elseStmt);
            currentBlock.addInstruction(new BranchInst(endLabel));
        }
        
        // End block
        currentBlock = new IRBasicBlock(endLabel);
        currentFunction.addBasicBlock(currentBlock);
    }
    
    private void visitForLoop(ForLoopNode node) {
        // for '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
        ForStmtNode initStmt = null;
        CondNode cond = null;
        ForStmtNode stepStmt = null;
        STMTnode bodyStmt = null;
        
        for (ASTnode child : node.children) {
            if (child instanceof ForStmtNode) {
                if (initStmt == null) {
                    initStmt = (ForStmtNode) child;
                } else {
                    stepStmt = (ForStmtNode) child;
                }
            } else if (child instanceof CondNode) {
                cond = (CondNode) child;
            } else if (child instanceof STMTnode) {
                bodyStmt = (STMTnode) child;
            }
        }
        
        String condLabel = newLabel("for_cond");
        String bodyLabel = newLabel("for_body");
        String stepLabel = newLabel("for_step");
        String endLabel = newLabel("for_end");
        
        breakLabels.push(endLabel);
        continueLabels.push(stepLabel);
        
        // Init
        if (initStmt != null) {
            visitForStmt(initStmt);
        }
        currentBlock.addInstruction(new BranchInst(condLabel));
        
        // Condition
        currentBlock = new IRBasicBlock(condLabel);
        currentFunction.addBasicBlock(currentBlock);
        if (cond != null) {
            visitCond(cond, bodyLabel, endLabel);
        } else {
            currentBlock.addInstruction(new BranchInst(bodyLabel));
        }
        
        // Body
        currentBlock = new IRBasicBlock(bodyLabel);
        currentFunction.addBasicBlock(currentBlock);
        if (bodyStmt != null) {
            visitStmt(bodyStmt);
        }
        currentBlock.addInstruction(new BranchInst(stepLabel));
        
        // Step
        currentBlock = new IRBasicBlock(stepLabel);
        currentFunction.addBasicBlock(currentBlock);
        if (stepStmt != null) {
            visitForStmt(stepStmt);
        }
        currentBlock.addInstruction(new BranchInst(condLabel));
        
        // End
        currentBlock = new IRBasicBlock(endLabel);
        currentFunction.addBasicBlock(currentBlock);
        
        breakLabels.pop();
        continueLabels.pop();
    }
    
    private void visitForStmt(ForStmtNode node) {
        // ForStmt -> LVal '=' Exp { ',' LVal '=' Exp }
        for (int i = 0; i < node.children.size(); i += 4) {
            if (i < node.children.size()) {
                LValNode lval = (LValNode) node.children.get(i);
                if (i + 2 < node.children.size()) {
                    EXPnode exp = (EXPnode) node.children.get(i + 2);
                    IRValue value = visitExp(exp);
                    IRValue ptr = visitLValAsPointer(lval);
                    currentBlock.addInstruction(new StoreInst(value, ptr));
                }
            }
        }
    }
    
    private void visitBreakStmt(BreakStmtNode node) {
        if (!breakLabels.isEmpty()) {
            currentBlock.addInstruction(new BranchInst(breakLabels.peek()));
        }
    }
    
    private void visitContinueStmt(ContinueStmtNode node) {
        if (!continueLabels.isEmpty()) {
            currentBlock.addInstruction(new BranchInst(continueLabels.peek()));
        }
    }
    
    private void visitReturnStmt(ReturnStmtNode node) {
        // return [Exp] ';'
        boolean hasExp = node.children.size() > 2;
        
        if (hasExp) {
            EXPnode exp = (EXPnode) node.children.get(1);
            IRValue value = visitExp(exp);
            currentBlock.addInstruction(new RetInst(IRType.I32, value));
        } else {
            currentBlock.addInstruction(new RetInst());
        }
    }
    
    private void visitPrintfStmt(PrintfStmtNode node) {
        // printf '(' StringConst { ',' Exp } ')' ';'
        StringConstNode formatNode = (StringConstNode) node.children.get(2);
        String formatString = formatNode.token.getValue();
        
        // Remove quotes
        if (formatString.startsWith("\"") && formatString.endsWith("\"")) {
            formatString = formatString.substring(1, formatString.length() - 1);
        }
        
        // Get expressions
        ArrayList<IRValue> expValues = new ArrayList<>();
        for (ASTnode child : node.children) {
            if (child instanceof EXPnode) {
                expValues.add(visitExp((EXPnode) child));
            }
        }
        
        // Process format string
        int expIndex = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < formatString.length(); i++) {
            char c = formatString.charAt(i);
            if (c == '%' && i + 1 < formatString.length() && formatString.charAt(i + 1) == 'd') {
                // Output accumulated string
                if (sb.length() > 0) {
                    outputString(sb.toString());
                    sb.setLength(0);
                }
                // Output integer
                if (expIndex < expValues.size()) {
                    ArrayList<IRValue> args = new ArrayList<>();
                    args.add(expValues.get(expIndex));
                    currentBlock.addInstruction(new CallInst(IRType.VOID, "putint", args));
                    expIndex++;
                }
                i++; // Skip 'd'
            } else if (c == '\\' && i + 1 < formatString.length()) {
                char next = formatString.charAt(i + 1);
                if (next == 'n') {
                    // Output accumulated string
                    if (sb.length() > 0) {
                        outputString(sb.toString());
                        sb.setLength(0);
                    }
                    // Output newline
                    ArrayList<IRValue> args = new ArrayList<>();
                    args.add(new IRValue(10)); // ASCII newline
                    currentBlock.addInstruction(new CallInst(IRType.VOID, "putch", args));
                    i++; // Skip 'n'
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        
        // Output remaining string
        if (sb.length() > 0) {
            outputString(sb.toString());
        }
    }
    
    private void outputString(String str) {
        // Create global string constant
        String strLabel = newStringLabel();
        String escapedStr = escapeString(str);
        int actualSize = str.length() + 1; // +1 for null terminator
        module.addGlobalString(strLabel, escapedStr, actualSize);
        
        // Get pointer to string
        String gepReg = newRegister();
        IRType strType = IRType.getArrayType(IRType.I8, actualSize);
        IRValue strPtr = new IRValue(IRType.getPointerType(strType), strLabel);
        ArrayList<IRValue> indices = new ArrayList<>();
        indices.add(new IRValue(0));
        indices.add(new IRValue(0));
        IRValue gepResult = new IRValue(IRType.getPointerType(IRType.I8), gepReg);
        
        currentBlock.addInstruction(new GetElementPtrInst(gepResult, strType, strPtr, indices));
        
        // Call putstr
        ArrayList<IRValue> args = new ArrayList<>();
        args.add(gepResult);
        currentBlock.addInstruction(new CallInst(IRType.VOID, "putstr", args));
    }
    
    private String escapeString(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c == '\n') {
                sb.append("\\0A");
            } else if (c == '\t') {
                sb.append("\\09");
            } else if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '"') {
                sb.append("\\22");
            } else {
                sb.append(c);
            }
        }
        sb.append("\\00"); // Null terminator
        return sb.toString();
    }
    
    private IRValue visitExp(EXPnode node) {
        if (node.children.isEmpty()) return new IRValue(0);
        return visitAddExp((AddExpNode) node.children.get(0));
    }
    
    private IRValue visitConstExp(ConstExpNode node) {
        if (node.children.isEmpty()) return new IRValue(0);
        return visitAddExp((AddExpNode) node.children.get(0));
    }
    
    private IRValue visitCond(CondNode node, String trueLabel, String falseLabel) {
        if (node.children.isEmpty()) {
            currentBlock.addInstruction(new BranchInst(trueLabel));
            return null;
        }
        return visitLOrExp((LOrExpNode) node.children.get(0), trueLabel, falseLabel);
    }
    
    private IRValue visitLOrExp(LOrExpNode node, String trueLabel, String falseLabel) {
        if (node.children.size() == 1) {
            // Single LAndExp
            return visitLAndExp((LAndExpNode) node.children.get(0), trueLabel, falseLabel);
        }
        
        // Multiple LAndExp with || (short-circuit)
        String nextLabel = newLabel("or_next");
        for (int i = 0; i < node.children.size(); i += 2) {
            if (node.children.get(i) instanceof LAndExpNode) {
                boolean isLast = (i >= node.children.size() - 2);
                String nextOr = isLast ? falseLabel : newLabel("or_next");
                visitLAndExp((LAndExpNode) node.children.get(i), trueLabel, nextOr);
                if (!isLast) {
                    currentBlock = new IRBasicBlock(nextOr);
                    currentFunction.addBasicBlock(currentBlock);
                }
            }
        }
        return null;
    }
    
    private IRValue visitLAndExp(LAndExpNode node, String trueLabel, String falseLabel) {
        if (node.children.size() == 1) {
            // Single EqExp
            IRValue value = visitEqExp((EqExpNode) node.children.get(0));
            String cmpReg = newRegister();
            IRValue cmpResult = new IRValue(IRType.I32, cmpReg);
            currentBlock.addInstruction(new IcmpInst(cmpResult, IcmpInst.Condition.NE, 
                                                      value, new IRValue(0)));
            currentBlock.addInstruction(new BranchInst(cmpResult, trueLabel, falseLabel));
            return value;
        }
        
        // Multiple EqExp with && (short-circuit)
        String nextLabel = newLabel("and_next");
        for (int i = 0; i < node.children.size(); i += 2) {
            if (node.children.get(i) instanceof EqExpNode) {
                boolean isLast = (i >= node.children.size() - 2);
                IRValue value = visitEqExp((EqExpNode) node.children.get(i));
                String cmpReg = newRegister();
                IRValue cmpResult = new IRValue(IRType.I32, cmpReg);
                currentBlock.addInstruction(new IcmpInst(cmpResult, IcmpInst.Condition.NE, 
                                                          value, new IRValue(0)));
                
                if (isLast) {
                    currentBlock.addInstruction(new BranchInst(cmpResult, trueLabel, falseLabel));
                } else {
                    String nextAnd = newLabel("and_next");
                    currentBlock.addInstruction(new BranchInst(cmpResult, nextAnd, falseLabel));
                    currentBlock = new IRBasicBlock(nextAnd);
                    currentFunction.addBasicBlock(currentBlock);
                }
            }
        }
        return null;
    }
    
    private IRValue visitAddExp(AddExpNode node) {
        if (node.children.size() == 1) {
            ASTnode child = node.children.get(0);
            if (child instanceof MulExpNode) {
                return visitMulExp((MulExpNode) child);
            } else if (child instanceof AddExpNode) {
                return visitAddExp((AddExpNode) child);
            }
        }
        
        IRValue left = null;
        TokenNode lastOp = null;
        
        for (int i = 0; i < node.children.size(); i++) {
            ASTnode child = node.children.get(i);
            
            if (child instanceof MulExpNode) {
                IRValue operand = visitMulExp((MulExpNode) child);
                if (left == null) {
                    left = operand;
                } else {
                    String resultReg = newRegister();
                    IRValue result = new IRValue(IRType.I32, resultReg);
                    
                    if (lastOp.token.getTokenType() == Token.TokenType.PLUS) {
                        currentBlock.addInstruction(new BinaryInst(result, BinaryInst.OpType.ADD, left, operand));
                    } else {
                        currentBlock.addInstruction(new BinaryInst(result, BinaryInst.OpType.SUB, left, operand));
                    }
                    left = result;
                }
            } else if (child instanceof AddExpNode) {
                IRValue operand = visitAddExp((AddExpNode) child);
                if (left == null) {
                    left = operand;
                } else {
                    String resultReg = newRegister();
                    IRValue result = new IRValue(IRType.I32, resultReg);
                    
                    if (lastOp.token.getTokenType() == Token.TokenType.PLUS) {
                        currentBlock.addInstruction(new BinaryInst(result, BinaryInst.OpType.ADD, left, operand));
                    } else {
                        currentBlock.addInstruction(new BinaryInst(result, BinaryInst.OpType.SUB, left, operand));
                    }
                    left = result;
                }
            } else if (child instanceof TokenNode) {
                lastOp = (TokenNode) child;
            }
        }
        
        return left;
    }
    
    private IRValue visitMulExp(MulExpNode node) {
        if (node.children.size() == 1) {
            ASTnode child = node.children.get(0);
            if (child instanceof UnaryExpNode) {
                return visitUnaryExp((UnaryExpNode) child);
            } else if (child instanceof MulExpNode) {
                return visitMulExp((MulExpNode) child);
            }
        }
        
        IRValue left = null;
        TokenNode lastOp = null;
        
        for (int i = 0; i < node.children.size(); i++) {
            ASTnode child = node.children.get(i);
            
            if (child instanceof UnaryExpNode) {
                IRValue operand = visitUnaryExp((UnaryExpNode) child);
                if (left == null) {
                    left = operand;
                } else {
                    String resultReg = newRegister();
                    IRValue result = new IRValue(IRType.I32, resultReg);
                    
                    Token.TokenType opType = lastOp.token.getTokenType();
                    if (opType == Token.TokenType.MULT) {
                        currentBlock.addInstruction(new BinaryInst(result, BinaryInst.OpType.MUL, left, operand));
                    } else if (opType == Token.TokenType.DIV) {
                        currentBlock.addInstruction(new BinaryInst(result, BinaryInst.OpType.SDIV, left, operand));
                    } else if (opType == Token.TokenType.MOD) {
                        currentBlock.addInstruction(new BinaryInst(result, BinaryInst.OpType.SREM, left, operand));
                    }
                    left = result;
                }
            } else if (child instanceof MulExpNode) {
                IRValue operand = visitMulExp((MulExpNode) child);
                if (left == null) {
                    left = operand;
                } else {
                    String resultReg = newRegister();
                    IRValue result = new IRValue(IRType.I32, resultReg);
                    
                    Token.TokenType opType = lastOp.token.getTokenType();
                    if (opType == Token.TokenType.MULT) {
                        currentBlock.addInstruction(new BinaryInst(result, BinaryInst.OpType.MUL, left, operand));
                    } else if (opType == Token.TokenType.DIV) {
                        currentBlock.addInstruction(new BinaryInst(result, BinaryInst.OpType.SDIV, left, operand));
                    } else if (opType == Token.TokenType.MOD) {
                        currentBlock.addInstruction(new BinaryInst(result, BinaryInst.OpType.SREM, left, operand));
                    }
                    left = result;
                }
            } else if (child instanceof TokenNode) {
                lastOp = (TokenNode) child;
            }
        }
        
        return left;
    }
    
    private IRValue visitUnaryExp(UnaryExpNode node) {
        if (node.children.isEmpty()) return new IRValue(0);
        
        ASTnode first = node.children.get(0);
        
        if (first instanceof PrimaryExpNode) {
            return visitPrimaryExp((PrimaryExpNode) first);
        }
        
        if (first instanceof UnaryOpNode) {
            UnaryOpNode opNode = (UnaryOpNode) first;
            UnaryExpNode unaryExp = (UnaryExpNode) node.children.get(1);
            IRValue value = visitUnaryExp(unaryExp);
            
            TokenNode opToken = (TokenNode) opNode.children.get(0);
            Token.TokenType opType = opToken.token.getTokenType();
            
            if (opType == Token.TokenType.PLUS) {
                return value;
            } else if (opType == Token.TokenType.MINU) {
                String resultReg = newRegister();
                IRValue result = new IRValue(IRType.I32, resultReg);
                currentBlock.addInstruction(new BinaryInst(result, BinaryInst.OpType.SUB, 
                                                            new IRValue(0), value));
                return result;
            } else if (opType == Token.TokenType.NOT) {
                String cmpReg = newRegister();
                IRValue cmpResult = new IRValue(IRType.I32, cmpReg);
                currentBlock.addInstruction(new IcmpInst(cmpResult, IcmpInst.Condition.EQ, 
                                                          value, new IRValue(0)));
                return cmpResult;
            }
        }
        
        // Function call: Ident '(' [FuncRParams] ')'
        if (first instanceof IdentNode) {
            IdentNode identNode = (IdentNode) first;
            String funcName = identNode.token.getValue();
            
            ArrayList<IRValue> args = new ArrayList<>();
            if (node.children.size() > 2 && node.children.get(2) instanceof FuncRParamsNode) {
                FuncRParamsNode paramsNode = (FuncRParamsNode) node.children.get(2);
                for (ASTnode child : paramsNode.children) {
                    if (child instanceof EXPnode) {
                        args.add(visitExp((EXPnode) child));
                    }
                }
            }
            
            // Check if it's a known function
            if (funcName.equals("getint")) {
                String resultReg = newRegister();
                IRValue result = new IRValue(IRType.I32, resultReg);
                currentBlock.addInstruction(new CallInst(result, IRType.I32, funcName, args));
                return result;
            } else {
                // User-defined function (assume returns int for now)
                String resultReg = newRegister();
                IRValue result = new IRValue(IRType.I32, resultReg);
                currentBlock.addInstruction(new CallInst(result, IRType.I32, funcName, args));
                return result;
            }
        }
        
        return new IRValue(0);
    }
    
    private IRValue visitPrimaryExp(PrimaryExpNode node) {
        if (node.children.isEmpty()) return new IRValue(0);
        
        ASTnode first = node.children.get(0);
        
        if (first instanceof TokenNode && 
            ((TokenNode)first).token.getTokenType() == Token.TokenType.LPARENT) {
            // '(' Exp ')'
            return visitExp((EXPnode) node.children.get(1));
        }
        
        if (first instanceof LValNode) {
            return visitLVal((LValNode) first);
        }
        
        if (first instanceof NumberNode) {
            return visitNumber((NumberNode) first);
        }
        
        return new IRValue(0);
    }
    
    private IRValue visitNumber(NumberNode node) {
        if (node.children.isEmpty()) return new IRValue(0);
        
        TokenNode tokenNode = (TokenNode) node.children.get(0);
        int value = Integer.parseInt(tokenNode.token.getValue());
        return new IRValue(value);
    }
    
    private IRValue visitLVal(LValNode node) {
        IRValue ptr = visitLValAsPointer(node);
        
        // Load value from pointer
        String loadReg = newRegister();
        IRValue loadResult = new IRValue(IRType.I32, loadReg);
        currentBlock.addInstruction(new LoadInst(loadResult, IRType.I32, ptr));
        return loadResult;
    }
    
    private IRValue visitLValAsPointer(LValNode node) {
        IdentNode identNode = (IdentNode) node.children.get(0);
        String varName = identNode.token.getValue();
        
        IRValue ptr = varMap.get(varName);
        if (ptr == null) {
            // Maybe global variable
            ptr = new IRValue(IRType.getPointerType(IRType.I32), "@" + varName);
        }
        
        return ptr;
    }
    
    private IRValue visitEqExp(EqExpNode node) {
        if (node.children.size() == 1) {
            ASTnode child = node.children.get(0);
            if (child instanceof RelExpNode) {
                return visitRelExp((RelExpNode) child);
            } else if (child instanceof EqExpNode) {
                return visitEqExp((EqExpNode) child);
            }
        }
        
        IRValue left = null;
        TokenNode lastOp = null;
        
        for (int i = 0; i < node.children.size(); i++) {
            ASTnode child = node.children.get(i);
            
            if (child instanceof RelExpNode) {
                IRValue operand = visitRelExp((RelExpNode) child);
                if (left == null) {
                    left = operand;
                } else {
                    String resultReg = newRegister();
                    IRValue result = new IRValue(IRType.I32, resultReg);
                    
                    if (lastOp.token.getTokenType() == Token.TokenType.EQL) {
                        currentBlock.addInstruction(new IcmpInst(result, IcmpInst.Condition.EQ, left, operand));
                    } else {
                        currentBlock.addInstruction(new IcmpInst(result, IcmpInst.Condition.NE, left, operand));
                    }
                    left = result;
                }
            } else if (child instanceof EqExpNode) {
                IRValue operand = visitEqExp((EqExpNode) child);
                if (left == null) {
                    left = operand;
                } else {
                    String resultReg = newRegister();
                    IRValue result = new IRValue(IRType.I32, resultReg);
                    
                    if (lastOp.token.getTokenType() == Token.TokenType.EQL) {
                        currentBlock.addInstruction(new IcmpInst(result, IcmpInst.Condition.EQ, left, operand));
                    } else {
                        currentBlock.addInstruction(new IcmpInst(result, IcmpInst.Condition.NE, left, operand));
                    }
                    left = result;
                }
            } else if (child instanceof TokenNode) {
                lastOp = (TokenNode) child;
            }
        }
        
        return left;
    }
    
    private IRValue visitRelExp(RelExpNode node) {
        if (node.children.size() == 1) {
            ASTnode child = node.children.get(0);
            if (child instanceof AddExpNode) {
                return visitAddExp((AddExpNode) child);
            } else if (child instanceof RelExpNode) {
                return visitRelExp((RelExpNode) child);
            }
        }
        
        IRValue left = null;
        TokenNode lastOp = null;
        
        for (int i = 0; i < node.children.size(); i++) {
            ASTnode child = node.children.get(i);
            
            if (child instanceof AddExpNode) {
                IRValue operand = visitAddExp((AddExpNode) child);
                if (left == null) {
                    left = operand;
                } else {
                    String resultReg = newRegister();
                    IRValue result = new IRValue(IRType.I32, resultReg);
                    
                    Token.TokenType opType = lastOp.token.getTokenType();
                    if (opType == Token.TokenType.LSS) {
                        currentBlock.addInstruction(new IcmpInst(result, IcmpInst.Condition.SLT, left, operand));
                    } else if (opType == Token.TokenType.LEQ) {
                        currentBlock.addInstruction(new IcmpInst(result, IcmpInst.Condition.SLE, left, operand));
                    } else if (opType == Token.TokenType.GRE) {
                        currentBlock.addInstruction(new IcmpInst(result, IcmpInst.Condition.SGT, left, operand));
                    } else if (opType == Token.TokenType.GEQ) {
                        currentBlock.addInstruction(new IcmpInst(result, IcmpInst.Condition.SGE, left, operand));
                    }
                    left = result;
                }
            } else if (child instanceof RelExpNode) {
                IRValue operand = visitRelExp((RelExpNode) child);
                if (left == null) {
                    left = operand;
                } else {
                    String resultReg = newRegister();
                    IRValue result = new IRValue(IRType.I32, resultReg);
                    
                    Token.TokenType opType = lastOp.token.getTokenType();
                    if (opType == Token.TokenType.LSS) {
                        currentBlock.addInstruction(new IcmpInst(result, IcmpInst.Condition.SLT, left, operand));
                    } else if (opType == Token.TokenType.LEQ) {
                        currentBlock.addInstruction(new IcmpInst(result, IcmpInst.Condition.SLE, left, operand));
                    } else if (opType == Token.TokenType.GRE) {
                        currentBlock.addInstruction(new IcmpInst(result, IcmpInst.Condition.SGT, left, operand));
                    } else if (opType == Token.TokenType.GEQ) {
                        currentBlock.addInstruction(new IcmpInst(result, IcmpInst.Condition.SGE, left, operand));
                    }
                    left = result;
                }
            } else if (child instanceof TokenNode) {
                lastOp = (TokenNode) child;
            }
        }
        
        return left;
    }
}
