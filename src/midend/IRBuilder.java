package midend;

import frontend.Token;
import frontend.ast.*;
import frontend.ast.exp.*;
import frontend.ast.stmt.*;
import frontend.ast.terminal.*;
import midend.llvmIR.Module;
import midend.llvmIR.Value;
import midend.llvmIR.type.*;
import midend.llvmIR.value.*;
import midend.llvmIR.value.instr.*;
import midend.llvmIR.value.constant.*;
import midend.symbol.SymbolTable;
import midend.symbol.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


public class IRBuilder {
    public Module module = new Module();

    private Function currentFunction = null;
    private BasicBlock currentBlock = null;
    private boolean isGlobalScope = true;
    private SymbolTable symbolTable;

    private Stack<BasicBlock> loopBreakTargets = new Stack<>();
    private Stack<BasicBlock> loopContinueTargets = new Stack<>();

    private Function getintFunc;
    private Function putintFunc;
    private Function putchFunc;
    private Function putstrFunc;

    private int regCounter = 0;
    private Map<String, Integer> labelCounters = new HashMap<>();

    public IRBuilder(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        initializeLibFuncs();
    }

    public Module getModule() {
        return module;
    }

    /**
     * 初始化库函数: getint, putint, putch, putstr
     * 这些函数声明为库函数（isLibrary=true），输出为declare而非define
     */
    private void initializeLibFuncs() {
        symbolTable.setCurrentScope(symbolTable.getRoot());

        // i32 @getint()
        getintFunc = new Function("getint", new FuncType(new IntegerType(32), new ArrayList<>()), true);
        module.addFunction(getintFunc);
        updateSymbolTable("getint", getintFunc);

        // void @putint(i32 %i)
        ArrayList<Type> putintParams = new ArrayList<>();
        putintParams.add(new IntegerType(32));
        putintFunc = new Function("putint", new FuncType(new VoidType(), putintParams), true);
        putintFunc.addArgument(new Argument("%i", new IntegerType(32), putintFunc));
        module.addFunction(putintFunc);
        updateSymbolTable("putint", putintFunc);

        // void @putch(i32 %i)
        ArrayList<Type> putchParams = new ArrayList<>();
        putchParams.add(new IntegerType(32));
        putchFunc = new Function("putch", new FuncType(new VoidType(), putchParams), true);
        putchFunc.addArgument(new Argument("%i", new IntegerType(32), putchFunc));
        module.addFunction(putchFunc);
        updateSymbolTable("putch", putchFunc);

        // void @putstr(i8* %str)
        ArrayList<Type> putstrParams = new ArrayList<>();
        putstrParams.add(new PointerType(new IntegerType(8)));
        putstrFunc = new Function("putstr", new FuncType(new VoidType(), putstrParams), true);
        putstrFunc.addArgument(new Argument("%str", new PointerType(new IntegerType(8)), putstrFunc));
        module.addFunction(putstrFunc);
        updateSymbolTable("putstr", putstrFunc);
    }


    private void updateSymbolTable(String name, Value irValue) {
        Symbol sym = symbolTable.findSymbol(name);
        if (sym != null) {
            sym.setIrValue(irValue);
        }
    }

    private String nextReg() {
        return "%" + (regCounter++);
    }

    private void resetLabelCounter() {
        labelCounters.clear();
    }

    private String getUniqueName(String prefix) {
        int next = labelCounters.getOrDefault(prefix, 0);
        labelCounters.put(prefix, next + 1);
        return prefix + "_" + next;
    }


    private BasicBlock createDelayBlock(String name) {
        BasicBlock block = new BasicBlock(getUniqueName(name), currentFunction);
        currentFunction.getBlocks().remove(block);  // 从自动添加中移除
        return block;
    }

    public void irVisit(CompUnitNode node) {
        symbolTable.setCurrentScope(symbolTable.getRoot());
        isGlobalScope = true;

        for (ASTnode child : node.children) {
            if (child instanceof DeclNode) {
                irVisit((DeclNode) child);
            } else if (child instanceof FuncDefNode) {
                isGlobalScope = false;
                irVisit((FuncDefNode) child);
                isGlobalScope = true;
            } else if (child instanceof MainFuncDefNode) {
                isGlobalScope = false;
                irVisit((MainFuncDefNode) child);
                isGlobalScope = true;
            }
        }
    }

    // ========== 声明处理 ==========

    public void irVisit(DeclNode node) {
        ASTnode child = node.children.get(0);
        if (child instanceof VarDeclNode) irVisit((VarDeclNode) child);
        else if (child instanceof ConstDeclNode) irVisit((ConstDeclNode) child);
    }

    public void irVisit(VarDeclNode node) {
        for (ASTnode child : node.children) {
            if (child instanceof VarDefNode) irVisit((VarDefNode) child);
        }
    }

    public void irVisit(ConstDeclNode node) {
        for (ASTnode child : node.children) {
            if (child instanceof ConstDefNode) irVisit((ConstDefNode) child);
        }
    }

    public void irVisit(VarDefNode node) {
        handleVarOrConstDef(node, false);
    }

    public void irVisit(ConstDefNode node) {
        handleVarOrConstDef(node, true);
    }

    private void handleVarOrConstDef(ASTnode node, boolean isConst) {
        IdentNode ident = (IdentNode) node.children.get(0);
        String name = ident.token.getValue();
        Symbol symbol = symbolTable.findSymbol(name);
        if (symbol == null) {
            throw new RuntimeException("符号 " + name + " 不存在于符号表");
        }

        boolean isArray = isArraySymbol(symbol);
        int arrayLen = resolveArrayLength(node, isArray);
        ASTnode initNode = extractInitNode(node);

        Integer constScalarValue = null;
        if (isConst && !isArray) {
            constScalarValue = evalConstInit(initNode);
            symbol.setConstValue(constScalarValue);
        }

        if (isGlobalScope) {
            handleGlobalDefinition(symbol, name, isConst, isArray, arrayLen, initNode, constScalarValue);
        } else {
            handleLocalDefinition(symbol, isConst, isArray, arrayLen, initNode, constScalarValue);
        }
    }

    private boolean isArraySymbol(Symbol symbol) {
        return symbol != null && symbol.getDimension() > 0;
    }

    private int resolveArrayLength(ASTnode node, boolean isArray) {
        if (!isArray) return 0;
        int len = extractArrayLength(node);
        return len > 0 ? len : 1;
    }

    private ASTnode extractInitNode(ASTnode node) {
        if (node.children.isEmpty()) return null;
        ASTnode last = node.children.get(node.children.size() - 1);
        if (last instanceof InitValNode || last instanceof ConstInitValNode) return last;
        return null;
    }

    private void handleGlobalDefinition(Symbol symbol, String name, boolean isConst,
                                        boolean isArray, int arrayLen, ASTnode initNode,
                                        Integer constScalarValue) {
        String globalName = "g_" + name;
        if (!isArray) {
            Constant initVal = constScalarValue != null
                    ? new ConstantInt(constScalarValue)
                    : buildScalarConstant(initNode);
            GlobalVar globalVar = new GlobalVar(globalName, initVal, isConst);
            module.addGlobalVar(globalVar);
            symbol.setIrValue(globalVar);
            return;
        }

        ArrayType arrayType = new ArrayType(new IntegerType(32), arrayLen);
        Constant arrayInit = buildGlobalArrayInit(arrayType, initNode, arrayLen);
        GlobalVar globalVar = new GlobalVar(globalName, arrayInit, isConst);
        module.addGlobalVar(globalVar);
        symbol.setIrValue(globalVar);
    }

    private Constant buildScalarConstant(ASTnode initNode) {
        if (initNode == null) return new ConstantInt(0);
        return new ConstantInt(evalConstInit(initNode));
    }

    private Constant buildGlobalArrayInit(ArrayType arrayType, ASTnode initNode, int arrayLen) {
        if (initNode == null) {
            return ConstantArray.zero(arrayType);
        }
        ArrayList<Integer> values = new ArrayList<>();
        collectConstInitValues(initNode, values);
        ArrayList<Constant> elements = convertToConstantList(values, arrayLen);
        return ConstantArray.of(arrayType, elements);
    }

    private void handleLocalDefinition(Symbol symbol, boolean isConst, boolean isArray,
                                       int arrayLen, ASTnode initNode, Integer constScalarValue) {
        if (!isArray) {
            Instruction alloca = new Instruction.AllocaInst(new IntegerType(32), currentBlock);
            alloca.setName(nextReg());
            symbol.setIrValue(alloca);
            if (initNode != null) {
                Value val;
                if (isConst && constScalarValue != null) {
                    val = new ConstantInt(constScalarValue);
                } else if (isConst) {
                    val = irVisit((ConstInitValNode) initNode);
                } else {
                    val = irVisit((InitValNode) initNode);
                }
                new Instruction.StoreInst(val, alloca, currentBlock);
            }
            return;
        }

        ArrayType arrayType = new ArrayType(new IntegerType(32), arrayLen);
        Instruction alloca = new Instruction.AllocaInst(arrayType, currentBlock);
        alloca.setName(nextReg());
        symbol.setIrValue(alloca);

        if (initNode == null) return;
        if (isConst) initLocalArrayWithConst(alloca, initNode, arrayLen);
        else initLocalArrayWithExprs(alloca, initNode, arrayLen);
    }

    private void initLocalArrayWithExprs(Value basePtr, ASTnode initNode, int arrayLen) {
        ArrayList<EXPnode> initExps = new ArrayList<>();
        collectInitExpNodes(initNode, initExps);
        for (int idx = 0; idx < initExps.size() && idx < arrayLen; idx++) {
            Value val = irVisitExp(initExps.get(idx));
            Instruction.GetElementPtrInst gep = buildArrayElementGep(basePtr, new ConstantInt(idx));
            gep.setName(nextReg());
            new Instruction.StoreInst(val, gep, currentBlock);
        }
    }

    private void initLocalArrayWithConst(Value basePtr, ASTnode initNode, int arrayLen) {
        ArrayList<Integer> values = new ArrayList<>();
        collectConstInitValues(initNode, values);
        for (int idx = 0; idx < values.size() && idx < arrayLen; idx++) {
            Instruction.GetElementPtrInst gep = buildArrayElementGep(basePtr, new ConstantInt(idx));
            gep.setName(nextReg());
            new Instruction.StoreInst(new ConstantInt(values.get(idx)), gep, currentBlock);
        }
    }

    // 递归计算常量初始化值（编译期求值）
    private int evalConstInit(ASTnode node) {
        Integer val = evalConstInitInternal(node);
        return val == null ? 0 : val;
    }

    private Integer evalConstInitInternal(ASTnode node) {
        if (node == null) return null;
        if (node instanceof ConstInitValNode || node instanceof InitValNode) {
            for (ASTnode child : node.children) {
                Integer v = evalConstInitInternal(child);
                if (v != null) return v;
            }
            return null;
        }
        if (node instanceof ConstExpNode) {
            return evalConstExp(node);
        }
        if (node instanceof NumberNode) {
            TokenNode tok = (TokenNode) node.children.get(0);
            return Integer.parseInt(tok.token.getValue());
        }
        if (node instanceof LValNode) {
            IdentNode ident = (IdentNode) node.children.get(0);
            Symbol symbol = symbolTable.findSymbol(ident.token.getValue());
            if (symbol != null && symbol.getType().isConst()) {
                Integer cached = symbol.getConstValue();
                if (cached != null && node.children.size() == 1) {
                    return cached;
                }
            }
            return null;
        }
        if (node instanceof EXPnode) {
            if (!node.children.isEmpty()) return evalConstExp(node.children.get(0));
            return null;
        }
        if (node instanceof TokenNode) {
            Token token = ((TokenNode) node).token;
            if (token.getTokenType() == Token.TokenType.INTCON) {
                return Integer.parseInt(token.getValue());
            }
        }

        for (ASTnode child : node.children) {
            Integer v = evalConstInitInternal(child);
            if (v != null) return v;
        }
        return null;
    }

    // 计算 ConstExp / Exp 的整数值
    private int evalConstExp(ASTnode node) {
        if (node == null) return 0;

        if (node instanceof NumberNode) {
            TokenNode tok = (TokenNode) node.children.get(0);
            return Integer.parseInt(tok.token.getValue());
        }
        if (node instanceof PrimaryExpNode) {
            if (node.children.size() == 3) {
                return evalConstExp(node.children.get(1));
            }
                return evalConstExp(node.children.get(0));
        }
        if (node instanceof AddExpNode) {
            if (node.children.size() == 1) return evalConstExp(node.children.get(0));
            int left = evalConstExp(node.children.get(0));
            int right = evalConstExp(node.children.get(2));
            String op = ((TokenNode) node.children.get(1)).token.getValue();
            return op.equals("+") ? left + right : left - right;
        }
        if (node instanceof MulExpNode) {
            if (node.children.size() == 1) return evalConstExp(node.children.get(0));
            int left = evalConstExp(node.children.get(0));
            int right = evalConstExp(node.children.get(2));
            String op = ((TokenNode) node.children.get(1)).token.getValue();
            return switch (op) {
                case "*" -> left * right;
                case "/" -> right == 0 ? 0 : left / right;
                default -> right == 0 ? 0 : left % right;
            };
        }
        if (node instanceof UnaryExpNode) {
            ASTnode first = node.children.get(0);
            if (first instanceof PrimaryExpNode) {
                return evalConstExp(first);
            }
            if (first instanceof UnaryOpNode) {
                String op = ((TokenNode) first.children.get(0)).token.getValue();
                int val = evalConstExp(node.children.get(1));
                if (op.equals("-")) return -val;
                if (op.equals("+")) return val;
                return val == 0 ? 1 : 0;
            }
            return 0;
        }
        if (node instanceof TokenNode) {
            Token token = ((TokenNode) node).token;
            if (token.getTokenType() == Token.TokenType.INTCON) {
                return Integer.parseInt(token.getValue());
            }
        }
        if (node instanceof LValNode) {
            IdentNode ident = (IdentNode) node.children.get(0);
            Symbol symbol = symbolTable.findSymbol(ident.token.getValue());
            if (symbol != null && symbol.getType().isConst() && node.children.size() == 1) {
                Integer cached = symbol.getConstValue();
                if (cached != null) return cached;
            }
            return 0;
        }
        if (node instanceof EXPnode) {
            if (!node.children.isEmpty()) return evalConstExp(node.children.get(0));
            return 0;
        }
        for (ASTnode child : node.children) {
            if (child != null) return evalConstExp(child);
        }
        return 0;
    }

    private int extractArrayLength(ASTnode node) {
        for (ASTnode child : node.children) {
            if (child instanceof ConstExpNode) {
                return evalConstExp(child);
            }
        }
        return 0;
    }

    private void collectInitExpNodes(ASTnode node, ArrayList<EXPnode> out) {
        if (node == null) return;
        if (node instanceof EXPnode) {
            out.add((EXPnode) node);
            return;
        }
        for (ASTnode child : node.children) {
            if (child instanceof TokenNode) continue;
            collectInitExpNodes(child, out);
        }
    }

    private void collectConstInitValues(ASTnode node, ArrayList<Integer> out) {
        if (node == null) return;
        if (node instanceof ConstExpNode) {
            out.add(evalConstExp(node));
            return;
        }
        if (node instanceof EXPnode) {
            if (!node.children.isEmpty()) out.add(evalConstExp(node.children.get(0)));
            return;
        }
        for (ASTnode child : node.children) {
            if (child instanceof TokenNode) continue;
            collectConstInitValues(child, out);
        }
    }

    private ArrayList<Constant> convertToConstantList(ArrayList<Integer> values, int total) {
        ArrayList<Constant> list = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            int v = i < values.size() ? values.get(i) : 0;
            list.add(new ConstantInt(v));
        }
        return list;
    }

    private Instruction.GetElementPtrInst buildArrayElementGep(Value basePtr, Value indexVal) {
        ArrayList<Value> idxs = new ArrayList<>();
        if (basePtr.getType() instanceof PointerType) {
            Type pointed = ((PointerType) basePtr.getType()).getPointedType();
            if (pointed instanceof ArrayType) {
                idxs.add(new ConstantInt(0));
            }
        }
        idxs.add(indexVal);
        return new Instruction.GetElementPtrInst(basePtr, idxs, currentBlock);
    }

    private boolean isArrayParam(FuncFParamNode node) {
        for (ASTnode child : node.children) {
            if (child instanceof TokenNode && ((TokenNode) child).token.getTokenType() == Token.TokenType.LBRACK) {
                return true;
            }
        }
        return false;
    }

    public Value irVisit(InitValNode node) {
        return irVisitExp(node.children.get(0));
    }

    public Value irVisit(ConstInitValNode node) {

        return irVisitExp(node.children.get(0));
    }


    public void irVisit(FuncDefNode node) {
        FuncTypeNode funcTypeNode = (FuncTypeNode) node.children.get(0);
        IdentNode identNode = (IdentNode) node.children.get(1);
        String funcName = identNode.token.getValue();

        ArrayList<Type> paramTypes = new ArrayList<>();
        ArrayList<Boolean> paramIsArray = new ArrayList<>();
        ASTnode paramsNode = null;
        if (node.children.get(3) instanceof FuncFParamsNode) {
            paramsNode = node.children.get(3);
            for(int i=0; i<paramsNode.children.size(); i+=2) {
                FuncFParamNode paramNode = (FuncFParamNode) paramsNode.children.get(i);
                boolean isArrayParam = isArrayParam(paramNode);
                paramIsArray.add(isArrayParam);
                if (isArrayParam) {
                    paramTypes.add(new PointerType(new IntegerType(32)));
                } else {
                    paramTypes.add(new IntegerType(32));
                }
            }
        }

        Type returnType = funcTypeNode.getTokenValue().equals("void") ? new VoidType() : new IntegerType(32);
        FuncType irFuncType = new FuncType(returnType, paramTypes);
        Function function = new Function(funcName, irFuncType, false);

        for (Type paramType : paramTypes) {
            Argument arg = new Argument("", paramType, function); // 名字稍后设置
            function.addArgument(arg);
        }
        

        updateSymbolTable(funcName, function);

        int pIdx = 0;
        for(Argument arg : function.getArguments()) {
            arg.setName("%a" + (pIdx++));
        }

        module.addFunction(function);

        // 设置当前函数上下文
        this.currentFunction = function;
        this.regCounter = 0;  // 重置寄存器计数（参数用%a不占%v）
        resetLabelCounter();

        if(node.scope != null) symbolTable.setCurrentScope(node.scope);

        // 创建入口基本块
        this.currentBlock = new BasicBlock("entry", function);

        // 处理参数：为每个参数分配栈空间并存储
        // 这样参数可以像局部变量一样被load/store
        ArrayList<Argument> args = function.getArguments();
        if (paramsNode != null) {
            int argIdx = 0;
            for(int i=0; i<paramsNode.children.size(); i+=2) {
                FuncFParamNode param = (FuncFParamNode) paramsNode.children.get(i);
                IdentNode paramIdent = (IdentNode) param.children.get(1);
                String paramName = paramIdent.token.getValue();

                Symbol paramSym = symbolTable.findSymbol(paramName);
                Argument arg = args.get(argIdx++);
                boolean arrayParam = argIdx - 1 < paramIsArray.size() && paramIsArray.get(argIdx - 1);

                if (arrayParam) {
                    // 数组形参退化为指针，直接保存指针供后续GEP使用
                    if (paramSym != null) paramSym.setIrValue(arg);
                } else {
                    // %v0 = alloca i32
                    // store i32 %a0, i32* %v0
                    Instruction alloca = new Instruction.AllocaInst(new IntegerType(32), currentBlock);
                    alloca.setName(nextReg());
                    new Instruction.StoreInst(arg, alloca, currentBlock);
                    if(paramSym != null) paramSym.setIrValue(alloca);
                }
            }
        }

        // 访问函数体
        ASTnode blockNode = node.children.get(node.children.size() - 1);
        irVisit((BlockNode) blockNode);

        // 补充默认返回（如果函数末尾没有return）
        if (returnType.isVoid() && !currentBlock.isTerminated()) {
            new Instruction.RetInst(currentBlock);
        } else if (!returnType.isVoid() && !currentBlock.isTerminated()) {
            new Instruction.RetInst(new ConstantInt(0), currentBlock);
        }

        symbolTable.exitScope();
    }


    public void irVisit(MainFuncDefNode node) {
        FuncType funcType = new FuncType(new IntegerType(32), new ArrayList<>());
        Function function = new Function("main", funcType, false);
        module.addFunction(function);
        updateSymbolTable("main", function);  // 关键：关联符号表

        this.currentFunction = function;
        this.regCounter = 0;  // 重置计数器
        resetLabelCounter();

        if(node.scope != null) symbolTable.setCurrentScope(node.scope);

        this.currentBlock = new BasicBlock("entry", function);

        // 访问main函数体
        ASTnode lastChild = node.children.get(node.children.size() - 1);
        if (lastChild instanceof BlockNode) {
            irVisit((BlockNode) lastChild);
        }

        // 补充默认返回 return 0;
        if (!currentBlock.isTerminated()) {
            new Instruction.RetInst(new ConstantInt(0), currentBlock);
        }

        symbolTable.exitScope();
    }


    public void irVisit(BlockNode node) {
        if(node.scope != null) symbolTable.setCurrentScope(node.scope);
        for (ASTnode child : node.children) {
            if (child instanceof BlockItemNode) {
                if (currentBlock.isTerminated()) break;  // 终结后停止
                irVisit((BlockItemNode) child);
            }
        }
        if(node.scope != null) symbolTable.exitScope();
    }


    public void irVisit(BlockItemNode node) {
        ASTnode child = node.children.get(0);
        if (child instanceof STMTnode) irVisit((STMTnode) child);
        else if (child instanceof DeclNode) irVisit((DeclNode) child);
    }

    public void irVisit(STMTnode node) {
        if (node.children.isEmpty()) return;
        ASTnode child = node.children.get(0);

        if (child instanceof ReturnStmtNode) irVisit((ReturnStmtNode) child);
        else if (child instanceof ExpStmtNode) irVisit((ExpStmtNode) child);
        else if (child instanceof BlockNode) irVisit((BlockNode) child);
        else if (child instanceof AssignStmtNode) irVisit((AssignStmtNode) child);
        else if (child instanceof IfStmtNode) irVisit((IfStmtNode) child);
        else if (child instanceof ForLoopNode) irVisit((ForLoopNode) child);
        else if (child instanceof ContinueStmtNode) irVisit((ContinueStmtNode) child);
        else if (child instanceof BreakStmtNode) irVisit((BreakStmtNode) child);
        else if (child instanceof PrintfStmtNode) irVisit((PrintfStmtNode) child);
    }


    public void irVisit(AssignStmtNode node) {
        LValNode lval = (LValNode) node.children.get(0);
        EXPnode exp = (EXPnode) node.children.get(2);
        Value rightVal = irVisitExp(exp);
        Value leftAddr = irVisitLValAddr(lval);
        new Instruction.StoreInst(rightVal, leftAddr, currentBlock);
    }


    public void irVisit(ExpStmtNode node) {
        if (!node.children.isEmpty()) {
            ASTnode child = node.children.get(0);
            if (child instanceof EXPnode) irVisitExp(child);
        }
    }


    public void irVisit(ReturnStmtNode node) {
        Value returnValue = null;
        if (node.children.size() > 1 && node.children.get(1) instanceof EXPnode) {
            returnValue = irVisitExp(node.children.get(1));
        }
        if (returnValue != null) new Instruction.RetInst(returnValue, currentBlock);
        else new Instruction.RetInst(currentBlock);
    }

    public void irVisit(PrintfStmtNode node) {
        StringConstNode fmtNode = (StringConstNode) node.children.get(2);
        String fmt = fmtNode.token.getValue();
        fmt = fmt.substring(1, fmt.length() - 1);

        int expIdx = 0;
        ArrayList<EXPnode> exps = new ArrayList<>();
        for(ASTnode child : node.children) {
            if(child instanceof EXPnode) exps.add((EXPnode)child);
        }

        for (int i = 0; i < fmt.length(); i++) {
            if (i < fmt.length() - 1 && fmt.charAt(i) == '%' && fmt.charAt(i + 1) == 'd') {
                Value val = irVisitExp(exps.get(expIdx++));
                ArrayList<Value> args = new ArrayList<>();
                args.add(val);
                new Instruction.CallInst(putintFunc, args, currentBlock).setName("");
                i++;
            } else if (fmt.charAt(i) == '\\' && i < fmt.length() - 1 && fmt.charAt(i + 1) == 'n') {
                ArrayList<Value> args = new ArrayList<>();
                args.add(new ConstantInt(10));
                new Instruction.CallInst(putchFunc, args, currentBlock).setName("");
                i++;
            } else {
                ArrayList<Value> args = new ArrayList<>();
                args.add(new ConstantInt(fmt.charAt(i)));
                new Instruction.CallInst(putchFunc, args, currentBlock).setName("");
            }
        }
    }


    private Value irVisitLValAddr(LValNode node) {
        IdentNode ident = (IdentNode) node.children.get(0);
        String name = ident.token.getValue();
        Symbol symbol = symbolTable.findSymbol(name);
        Value basePtr = symbol != null ? symbol.getIrValue() : null;
        if (basePtr == null) {
            throw new RuntimeException("变量 " + name + " 未绑定IR指针");
        }

        ArrayList<Value> indices = new ArrayList<>();
        for (int i = 1; i < node.children.size(); i++) {
            ASTnode child = node.children.get(i);
            if (child instanceof TokenNode && ((TokenNode) child).token.getTokenType() == Token.TokenType.LBRACK) {
                ASTnode expNode = node.children.get(i + 1);
                Value idxVal = (expNode instanceof EXPnode)
                        ? irVisitExp(expNode)
                        : new ConstantInt(evalConstExp(expNode));
                indices.add(idxVal);
            }
        }

        if (indices.isEmpty()) {
            return basePtr;
        }

        if (!(basePtr.getType() instanceof PointerType)) {
            throw new RuntimeException("变量 " + name + " 不是指针类型，无法执行GEP");
        }

        Type pointed = ((PointerType) basePtr.getType()).getPointedType();
        ArrayList<Value> gepIdx = new ArrayList<>();

        // 局部/全局数组的alloca或global返回 [n x i32]*，需要先索引0层
        if (pointed instanceof ArrayType) {
            gepIdx.add(new ConstantInt(0));
        }

        // SysY当前仅用到一维数组，存在多个索引时按顺序加入实现降级
        gepIdx.addAll(indices);

        Instruction.GetElementPtrInst gep = new Instruction.GetElementPtrInst(basePtr, gepIdx, currentBlock);
        gep.setName(nextReg());
        return gep;
    }

    private Value buildShortCircuitValue(ASTnode logicalNode) {
        BasicBlock trueBlock = createDelayBlock("bool_true");
        BasicBlock falseBlock = createDelayBlock("bool_false");
        BasicBlock mergeBlock = createDelayBlock("bool_merge");

        if (logicalNode instanceof LOrExpNode) {
            irVisitLOrExp((LOrExpNode) logicalNode, trueBlock, falseBlock);
        } else if (logicalNode instanceof LAndExpNode) {
            irVisitLAndExp((LAndExpNode) logicalNode, trueBlock, falseBlock);
        }

        ConstantInt one = new ConstantInt(1);
        ConstantInt zero = new ConstantInt(0);

        currentFunction.addBasicBlock(trueBlock);
        this.currentBlock = trueBlock;
        new Instruction.BrInst(mergeBlock, currentBlock);

        currentFunction.addBasicBlock(falseBlock);
        this.currentBlock = falseBlock;
        new Instruction.BrInst(mergeBlock, currentBlock);

        currentFunction.addBasicBlock(mergeBlock);
        this.currentBlock = mergeBlock;

        Instruction.PhiInst phi = new Instruction.PhiInst(new IntegerType(32), currentBlock);
        phi.setName(nextReg());
        phi.addIncoming(one, trueBlock);
        phi.addIncoming(zero, falseBlock);
        return phi;
    }

    private Value buildEqValue(EqExpNode node) {
        if (node.children.size() == 1) {
            return buildRelValue((RelExpNode) node.children.get(0));
        }

        Value left = buildEqValue((EqExpNode) node.children.get(0));
        Value right = buildRelValue((RelExpNode) node.children.get(2));
        String op = ((TokenNode) node.children.get(1)).token.getValue();
        Instruction.IcmpInst.Cond cond = op.equals("==") ?
                Instruction.IcmpInst.Cond.EQ : Instruction.IcmpInst.Cond.NE;
        Instruction.IcmpInst icmp = new Instruction.IcmpInst(cond, left, right, currentBlock);
        icmp.setName(nextReg());
        return zextBoolToI32(icmp);
    }

    private Value buildRelValue(RelExpNode node) {
        if (node.children.size() == 1) {
            return irVisitExp(node.children.get(0));
        }

        Value left = buildRelValue((RelExpNode) node.children.get(0));
        Value right = irVisitExp(node.children.get(2));
        String op = ((TokenNode) node.children.get(1)).token.getValue();
        Instruction.IcmpInst.Cond cond = switch (op) {
            case "<" -> Instruction.IcmpInst.Cond.SLT;
            case ">" -> Instruction.IcmpInst.Cond.SGT;
            case "<=" -> Instruction.IcmpInst.Cond.SLE;
            case ">=" -> Instruction.IcmpInst.Cond.SGE;
            default -> null;
        };
        if (cond == null) {
            throw new RuntimeException("未知的关系运算符: " + op);
        }
        Instruction.IcmpInst icmp = new Instruction.IcmpInst(cond, left, right, currentBlock);
        icmp.setName(nextReg());
        return zextBoolToI32(icmp);
    }

    private Value zextBoolToI32(Value boolVal) {
        Instruction.ZextInst zext = new Instruction.ZextInst(boolVal, new IntegerType(32), currentBlock);
        zext.setName(nextReg());
        return zext;
    }


    public Value irVisitExp(ASTnode node) {
        if (node instanceof CondNode) return irVisitExp(node.children.get(0));
        if (node instanceof LOrExpNode || node instanceof LAndExpNode) {
            return buildShortCircuitValue(node);
        }
        if (node instanceof EqExpNode) return buildEqValue((EqExpNode) node);
        if (node instanceof RelExpNode) return buildRelValue((RelExpNode) node);
        if (node instanceof AddExpNode) return irVisit((AddExpNode) node);
        if (node instanceof MulExpNode) return irVisit((MulExpNode) node);
        if (node instanceof UnaryExpNode) return irVisit((UnaryExpNode) node);
        if (node instanceof PrimaryExpNode) return irVisit((PrimaryExpNode) node);
        if (node instanceof LValNode) {
            Value addr = irVisitLValAddr((LValNode) node);
            Instruction.LoadInst load = new Instruction.LoadInst(addr, currentBlock);
            load.setName(nextReg());
            return load;
        }
        if (node instanceof NumberNode) return irVisit((NumberNode) node);
        if (!node.children.isEmpty()) return irVisitExp(node.children.get(0));
        return null;
    }


    public Value irVisit(AddExpNode node) {
        if (node.children.size() == 1) return irVisitExp(node.children.get(0));
        Value left = irVisitExp(node.children.get(0));
        Value right = irVisitExp(node.children.get(2));
        TokenNode opToken = (TokenNode) node.children.get(1);
        Instruction.BinaryInst.Op opcode = opToken.token.getValue().equals("+") ?
                Instruction.BinaryInst.Op.ADD : Instruction.BinaryInst.Op.SUB;
        Instruction.BinaryInst inst = new Instruction.BinaryInst(opcode, left, right, currentBlock);
        inst.setName(nextReg());
        return inst;
    }


    public Value irVisit(MulExpNode node) {
        if (node.children.size() == 1) return irVisitExp(node.children.get(0));
        Value left = irVisitExp(node.children.get(0));
        Value right = irVisitExp(node.children.get(2));
        String op = ((TokenNode) node.children.get(1)).token.getValue();
        Instruction.BinaryInst.Op opcode;
        if (op.equals("*")) opcode = Instruction.BinaryInst.Op.MUL;
        else if (op.equals("/")) opcode = Instruction.BinaryInst.Op.SDIV;
        else opcode = Instruction.BinaryInst.Op.SREM;
        Instruction.BinaryInst inst = new Instruction.BinaryInst(opcode, left, right, currentBlock);
        inst.setName(nextReg());
        return inst;
    }


    public Value irVisit(UnaryExpNode node) {
        ASTnode first = node.children.get(0);
        if (first instanceof PrimaryExpNode) return irVisit((PrimaryExpNode) first);

        // 函数调用
        if (first instanceof IdentNode) {
            String funcName = ((IdentNode) first).token.getValue();
            Symbol funcSym = symbolTable.findSymbol(funcName);
            
            if (funcSym == null) {
                throw new RuntimeException("函数 " + funcName + " 未定义");
            }
            
            Function func = (Function) funcSym.getIrValue();
            
            if (func == null) {
                throw new RuntimeException("函数 " + funcName + " 的 IR 值为空");
            }

            // 解析实参
            ArrayList<Value> args = new ArrayList<>();
            if (node.children.size() > 2 && node.children.get(2) instanceof FuncRParamsNode params) {
                for(int i=0; i<params.children.size(); i+=2) {
                    args.add(irVisitExp(params.children.get(i)));
                }
            }

            // 生成call指令
            Instruction.CallInst call = new Instruction.CallInst(func, args, currentBlock);
            if (!func.getType().isVoid()) call.setName(nextReg());
            else call.setName("");
            return call;
        }

        // 一元运算符
        if (first instanceof UnaryOpNode) {
            String op = ((TokenNode)first.children.get(0)).token.getValue();
            Value val = irVisitExp(node.children.get(1));
            switch (op) {
                case "-" -> {
                    // 负号：0 - val
                    Instruction.BinaryInst inst = new Instruction.BinaryInst(
                            Instruction.BinaryInst.Op.SUB, new ConstantInt(0), val, currentBlock);
                    inst.setName(nextReg());
                    return inst;
                }
                case "+" -> {
                    // 正号：直接返回
                    return val;
                    // 正号：直接返回
                }
                case "!" -> {
                    // 逻辑非：icmp eq val, 0 → zext i1 to i32
                    Instruction icmp = new Instruction.IcmpInst(
                            Instruction.IcmpInst.Cond.EQ, val, new ConstantInt(0), currentBlock);
                    icmp.setName(nextReg());
                    Instruction zext = new Instruction.ZextInst(
                            icmp, new IntegerType(32), currentBlock);
                    zext.setName(nextReg());
                    return zext;
                }
            }
        }
        return null;
    }


    public Value irVisit(PrimaryExpNode node) {
        if (node.children.size() == 3) return irVisitExp(node.children.get(1));
        ASTnode child = node.children.get(0);
        if (child instanceof NumberNode) return irVisit((NumberNode) child);
        if (child instanceof LValNode) {
            Value addr = irVisitLValAddr((LValNode) child);
            Instruction.LoadInst load = new Instruction.LoadInst(addr, currentBlock);
            load.setName(nextReg());
            return load;
        }
        return null;
    }


    public Value irVisit(NumberNode node) {
        int val = Integer.parseInt(((TokenNode) node.children.get(0)).token.getValue());
        return new ConstantInt(val);
    }


    public void irVisitCond(ASTnode node, BasicBlock trueBlock, BasicBlock falseBlock) {
        if (node instanceof CondNode) irVisitCond(node.children.get(0), trueBlock, falseBlock);
        else if (node instanceof LOrExpNode) irVisitLOrExp((LOrExpNode) node, trueBlock, falseBlock);
        else {
            // 非布尔表达式：转为i1再br
            Value val = irVisitExp(node);
            if (val.getType().isInteger1()) {
                new Instruction.BrInst(val, trueBlock, falseBlock, currentBlock);
            } else {
                Instruction icmp = new Instruction.IcmpInst(
                        Instruction.IcmpInst.Cond.NE, val, new ConstantInt(0), currentBlock);
                icmp.setName(nextReg());
                new Instruction.BrInst(icmp, trueBlock, falseBlock, currentBlock);
            }
        }
    }


    public void irVisitLOrExp(LOrExpNode node, BasicBlock trueBlock, BasicBlock falseBlock) {
        if (node.children.size() == 1) {
            irVisitLAndExp((LAndExpNode) node.children.get(0), trueBlock, falseBlock);
            return;
        }
        // 创建中间块：左侧为假时求值右侧
        BasicBlock checkRightBlock = createDelayBlock("or_next");

        irVisitLOrExp((LOrExpNode) node.children.get(0), trueBlock, checkRightBlock);

        currentFunction.addBasicBlock(checkRightBlock);
        this.currentBlock = checkRightBlock;

        irVisitLAndExp((LAndExpNode) node.children.get(2), trueBlock, falseBlock);
    }


    public void irVisitLAndExp(LAndExpNode node, BasicBlock trueBlock, BasicBlock falseBlock) {
        if (node.children.size() == 1) {
            irVisitEqExp((EqExpNode) node.children.get(0), trueBlock, falseBlock);
            return;
        }
        // 创建中间块：左侧为真时求值右侧
        BasicBlock checkRightBlock = createDelayBlock("and_next");

        irVisitLAndExp((LAndExpNode) node.children.get(0), checkRightBlock, falseBlock);

        currentFunction.addBasicBlock(checkRightBlock);
        this.currentBlock = checkRightBlock;

        irVisitEqExp((EqExpNode) node.children.get(2), trueBlock, falseBlock);
    }


    public void irVisitEqExp(EqExpNode node, BasicBlock trueBlock, BasicBlock falseBlock) {
        if (node.children.size() == 1) {
            irVisitRelExp((RelExpNode) node.children.get(0), trueBlock, falseBlock);
            return;
        }
        String op = ((TokenNode) node.children.get(1)).getToken().getValue();
        Instruction.IcmpInst.Cond cond = op.equals("==") ?
                Instruction.IcmpInst.Cond.EQ : Instruction.IcmpInst.Cond.NE;
        Value left = irVisitExp(node.children.get(0));
        Value right = irVisitExp(node.children.get(2));
        Instruction icmp = new Instruction.IcmpInst(cond, left, right, currentBlock);
        icmp.setName(nextReg());
        new Instruction.BrInst(icmp, trueBlock, falseBlock, currentBlock);
    }


    public void irVisitRelExp(RelExpNode node, BasicBlock trueBlock, BasicBlock falseBlock) {
        if (node.children.size() == 1) {
            // 单个值转布尔：val != 0
            Value val = irVisitExp(node.children.get(0));
            Instruction icmp = new Instruction.IcmpInst(
                    Instruction.IcmpInst.Cond.NE, val, new ConstantInt(0), currentBlock);
            icmp.setName(nextReg());
            new Instruction.BrInst(icmp, trueBlock, falseBlock, currentBlock);
            return;
        }
        String op = ((TokenNode) node.children.get(1)).getToken().getValue();
        Instruction.IcmpInst.Cond cond = switch (op) {
            case "<" -> Instruction.IcmpInst.Cond.SLT;
            case ">" -> Instruction.IcmpInst.Cond.SGT;
            case "<=" -> Instruction.IcmpInst.Cond.SLE;
            case ">=" -> Instruction.IcmpInst.Cond.SGE;
            default -> null;
        };
        Value left = irVisitExp(node.children.get(0));
        Value right = irVisitExp(node.children.get(2));
        Instruction icmp = new Instruction.IcmpInst(cond, left, right, currentBlock);
        icmp.setName(nextReg());
        new Instruction.BrInst(icmp, trueBlock, falseBlock, currentBlock);
    }


    public void irVisit(IfStmtNode node) {
        BasicBlock trueBlock = createDelayBlock("if_true");
        BasicBlock falseBlock = createDelayBlock("if_false");
        BasicBlock nextBlock = createDelayBlock("if_next");

        // 检查是否有else分支
        boolean hasElse = false;
        ASTnode elseStmt = null;
        for (int i = 0; i < node.children.size(); i++) {
            if (node.children.get(i) instanceof TokenNode &&
                    ((TokenNode)node.children.get(i)).getToken().getTokenType() == Token.TokenType.ELSETK) {
                hasElse = true;
                elseStmt = node.children.get(i + 1);
                break;
            }
        }
        BasicBlock falseTarget = hasElse ? falseBlock : nextBlock;

        // 求值条件并生成分支
        irVisitCond(node.children.get(2), trueBlock, falseTarget);

        // 生成true分支
        currentFunction.addBasicBlock(trueBlock);
        this.currentBlock = trueBlock;
        irVisit((STMTnode) node.children.get(4));
        if (!currentBlock.isTerminated()) new Instruction.BrInst(nextBlock, currentBlock);

        // 生成false分支（如果有else）
        if (hasElse) {
            currentFunction.addBasicBlock(falseBlock);
            this.currentBlock = falseBlock;
            irVisit((STMTnode) elseStmt);
            if (!currentBlock.isTerminated()) new Instruction.BrInst(nextBlock, currentBlock);
        }

        // 继续后续代码
        currentFunction.addBasicBlock(nextBlock);
        this.currentBlock = nextBlock;
    }


    public void irVisit(ForLoopNode node) {
        ASTnode initNode = null, condNode = null, updateNode = null, bodyNode = node.children.get(node.children.size() - 1);
        int semiCount = 0;
        for(ASTnode child : node.children) {
            if(child instanceof TokenNode && ((TokenNode)child).token.getTokenType() == Token.TokenType.SEMICN) {
                semiCount++;
            }
            else if(child instanceof ForStmtNode) {
                if(semiCount == 0) initNode = child;
                else updateNode = child;
            }
            else if(child instanceof CondNode) condNode = child;
        }

        if (initNode != null) irVisit((ForStmtNode) initNode);

        BasicBlock condBlock = new BasicBlock(getUniqueName("for_cond"), currentFunction);

        BasicBlock bodyBlock = createDelayBlock("for_body");
        BasicBlock updateBlock = createDelayBlock("for_update");
        BasicBlock nextBlock = createDelayBlock("for_next");

        loopBreakTargets.push(nextBlock);
        loopContinueTargets.push(updateBlock);

        new Instruction.BrInst(condBlock, currentBlock);

        this.currentBlock = condBlock;
        if (condNode != null) irVisitCond(condNode, bodyBlock, nextBlock);
        else new Instruction.BrInst(bodyBlock, currentBlock);

        currentFunction.addBasicBlock(bodyBlock);
        this.currentBlock = bodyBlock;
        irVisit((STMTnode) bodyNode);
        if (!currentBlock.isTerminated()) new Instruction.BrInst(updateBlock, currentBlock);

        currentFunction.addBasicBlock(updateBlock);
        this.currentBlock = updateBlock;
        if (updateNode != null) irVisit((ForStmtNode) updateNode);
        new Instruction.BrInst(condBlock, currentBlock);

        // 弹出break/continue目标
        loopBreakTargets.pop();
        loopContinueTargets.pop();

        // 继续后续代码
        currentFunction.addBasicBlock(nextBlock);
        this.currentBlock = nextBlock;
    }


    public void irVisit(BreakStmtNode node) {
        if (!loopBreakTargets.isEmpty()) new Instruction.BrInst(loopBreakTargets.peek(), currentBlock);
    }


    public void irVisit(ContinueStmtNode node) {
        if (!loopContinueTargets.isEmpty()) new Instruction.BrInst(loopContinueTargets.peek(), currentBlock);
    }


    public void irVisit(ForStmtNode node) {
        for (int i = 0; i < node.children.size(); i += 4) {
            LValNode lval = (LValNode) node.children.get(i);
            EXPnode exp = (EXPnode) node.children.get(i + 2);
            Value right = irVisitExp(exp);
            Value leftAddr = irVisitLValAddr(lval);
            new Instruction.StoreInst(right, leftAddr, currentBlock);
            if (i + 3 >= node.children.size()) break;
        }
    }


}