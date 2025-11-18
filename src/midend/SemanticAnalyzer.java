package midend;
import frontend.Token; // 需要访问 Token 类型
import java.util.ArrayList;
import frontend.ast.*;
import frontend.ast.ASTnode;
import frontend.ast.stmt.*;
import frontend.ast.exp.*;
import frontend.ast.terminal.*;
import midend.symbol.*;
import error.MyErrorHandler;
import error.CompileError.ErrorType; // 导入错误类型枚举

public class SemanticAnalyzer {
    private SymbolTable symbolTable = new SymbolTable();
    private MyErrorHandler errorHandler = MyErrorHandler.getInstance();


    private Symbol currentFunction = null;
    private int loopDepth = 0;

    public SemanticAnalyzer() {
        Symbol getintSymbol = new Symbol("getint", SymbolType.INT_FUNC, 0);
        symbolTable.addSymbol(getintSymbol);

        Symbol printfSymbol = new Symbol("printf", SymbolType.VOID_FUNC, 0);
        symbolTable.addSymbol(printfSymbol);
    }

    public SymbolTable getSymbolTable() { return symbolTable; }

    public SymbolType visit(ASTnode node) {
        for (ASTnode child : node.children) {
            if (child != null) {
                child.accept(this);
            }
        }
        return null;
    }

//    public SymbolType visit(CompUnitNode node) {
//        for (ASTnode child : node.children) {
//            if (child != null) child.accept(this);
//        }
//        return null;
//    }

//    public SymbolType visit(ConstDeclNode node) {
//        for (ASTnode child : node.children) {
//            if (child != null) child.accept(this);
//        }
//        return null;
//    }

    public SymbolType visit(ConstDefNode node) {
        IdentNode identNode =(IdentNode) node.children.get(0);
        if (identNode == null) return null;
        String name = identNode.token.getValue();
        int lineNum = identNode.token.lineNumber;
        if (symbolTable.checkRedefine(name)) {
            errorHandler.addError(lineNum, ErrorType.NAME_REDEFINITION);
        } else {
            boolean isArray = node.children.stream().anyMatch(n -> n instanceof TokenNode && ((TokenNode)n).token.getTokenType() == Token.TokenType.LBRACK);
            SymbolType type = isArray ? SymbolType.CONST_INT_ARRAY : SymbolType.CONST_INT;
            int dim = isArray ? 1 : 0;
            Symbol symbol = new Symbol(name, type, lineNum);
            symbol.setDimension(dim);
            
            // 标记是否为全局变量（scopeId为1表示全局）
            symbol.setGlobal(symbolTable.getCurrentScopeId() == 1);
            
            // 对于非数组常量，尝试提取常量值
            if (!isArray) {
                for (ASTnode child : node.children) {
                    if (child instanceof ConstInitValNode) {
                        Integer constVal = evaluateConstInitVal((ConstInitValNode) child);
                        if (constVal != null) {
                            symbol.setConstValue(constVal);
                        }
                        break;
                    }
                }
            }
            
            symbolTable.addSymbol(symbol);
        }
        for (ASTnode child : node.children) {
            if (child != null && child != identNode) {
                child.accept(this);
            }
        }
        return null;
    }
    
    // 辅助方法：计算常量初始值
    private Integer evaluateConstInitVal(ConstInitValNode node) {
        // 简单情况：ConstInitVal -> ConstExp
        for (ASTnode child : node.children) {
            if (child instanceof ConstExpNode) {
                return evaluateConstExp((ConstExpNode) child);
            }
        }
        return null;
    }
    
    // 辅助方法：计算常量表达式（简单实现）
    private Integer evaluateConstExp(ConstExpNode node) {
        // 这里只处理最简单的情况：直接的数字常量
        // 完整实现需要递归计算整个表达式
        for (ASTnode child : node.children) {
            if (child instanceof AddExpNode) {
                return evaluateAddExp((AddExpNode) child);
            }
        }
        return null;
    }
    
    private Integer evaluateAddExp(AddExpNode node) {
        for (ASTnode child : node.children) {
            if (child instanceof MulExpNode) {
                return evaluateMulExp((MulExpNode) child);
            }
        }
        return null;
    }
    
    private Integer evaluateMulExp(MulExpNode node) {
        for (ASTnode child : node.children) {
            if (child instanceof UnaryExpNode) {
                return evaluateUnaryExp((UnaryExpNode) child);
            }
        }
        return null;
    }
    
    private Integer evaluateUnaryExp(UnaryExpNode node) {
        for (ASTnode child : node.children) {
            if (child instanceof PrimaryExpNode) {
                return evaluatePrimaryExp((PrimaryExpNode) child);
            }
        }
        return null;
    }
    
    private Integer evaluatePrimaryExp(PrimaryExpNode node) {
        for (ASTnode child : node.children) {
            if (child instanceof NumberNode) {
                return evaluateNumber((NumberNode) child);
            }
        }
        return null;
    }
    
    private Integer evaluateNumber(NumberNode node) {
        // NumberNode contains a TokenNode child with the actual number
        for (ASTnode child : node.children) {
            if (child instanceof TokenNode) {
                TokenNode tokenNode = (TokenNode) child;
                try {
                    return Integer.parseInt(tokenNode.token.getValue());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

//    public SymbolType visit(ConstInitValNode node) {
//        // ConstInitVal -> ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}'
//        for (ASTnode child : node.children) {
//            if (child instanceof ConstExpNode) {
//                child.accept(this);
//            }
//        }
//        return null;
//    }
public SymbolType visit(VarDeclNode node) {
    boolean isStatic = node.children.get(0) instanceof TokenNode &&
            ((TokenNode)node.children.get(0)).token.getTokenType() == Token.TokenType.STATICTK;

    for (ASTnode child : node.children) {
        if (child instanceof VarDefNode) {
            visitVarDef((VarDefNode) child, isStatic);
        }
    }
    return null;
}

    public void visitVarDef(VarDefNode node, boolean isStatic) {
        IdentNode identNode =(IdentNode) node.children.get(0);
        if (identNode == null) return;
        String name = identNode.token.getValue();
        int lineNum = identNode.token.lineNumber;

        Symbol symbol = null;

        if (symbolTable.checkRedefine(name)) {
            errorHandler.addError(lineNum, ErrorType.NAME_REDEFINITION);
        } else {
            boolean isArray = node.children.stream().anyMatch(n -> n instanceof TokenNode && ((TokenNode)n).token.getTokenType() == Token.TokenType.LBRACK);
            SymbolType type;
            if (isStatic) {
                type = isArray ? SymbolType.STATIC_INT_ARRAY : SymbolType.STATIC_INT;
            } else {
                type = isArray ? SymbolType.INT_ARRAY : SymbolType.INT;
            }
            int dim = isArray ? 1 : 0;

            symbol = new Symbol(name, type, lineNum);
            symbol.setDimension(dim);
            
            // 标记是否为全局变量（scopeId为1表示全局）
            symbol.setGlobal(symbolTable.getCurrentScopeId() == 1);
            
            // 如果是数组，提取数组大小
            if (isArray) {
                for (ASTnode child : node.children) {
                    if (child instanceof ConstExpNode) {
                        Integer size = evaluateConstExp((ConstExpNode) child);
                        if (size != null) {
                            symbol.setArraySize(size);
                        }
                        break;
                    }
                }
            }
            
            symbolTable.addSymbol(symbol);
        }

        for (ASTnode child : node.children) {
            if (child != identNode) {
                child.accept(this);
            }
        }
    }

    public SymbolType visit(FuncDefNode node) {
        FuncTypeNode funcType = (FuncTypeNode) node.children.get(0);
        boolean isVoid = funcType.getTokenValue().equals("void");
        SymbolType type = isVoid ? SymbolType.VOID_FUNC:SymbolType.INT_FUNC;
        IdentNode identNode = (IdentNode) node.children.get(1);
        String name = identNode.token.getValue();
        int lineNum = identNode.token.lineNumber;

        Symbol funcSymbol = null;
        if (symbolTable.checkRedefine(name)) {
            errorHandler.addError(lineNum, ErrorType.NAME_REDEFINITION);
            funcSymbol = new Symbol(name, type, lineNum);
        } else {
            funcSymbol = new Symbol(name, type, lineNum);
            symbolTable.addSymbol(funcSymbol); // 添加到全局作用域
        }
        currentFunction = funcSymbol; // 设置当前函数
        symbolTable.enterScope();

        if(node.children.get(3) instanceof FuncFParamsNode){
            FuncFParamsNode funcFParamsNode = (FuncFParamsNode) node.children.get(3);
            visitFuncFParams(funcFParamsNode, funcSymbol);
             }

        BlockNode blockNode = (BlockNode) node.children.get(node.children.size()-1);
        for (int i = 1; i < blockNode.children.size() - 1; i++) {
            ASTnode blockItem = blockNode.children.get(i);
            if (blockItem != null) {
                blockItem.accept(this);
            }
        }

        if (type == SymbolType.INT_FUNC) {
            boolean hasReturn = false;
            // Block -> '{' {BlockItem} '}'
            if (blockNode.children.size() >= 2) {
                ASTnode lastItem = blockNode.children.get(blockNode.children.size() - 2);
                // BlockItem -> Stmt -> ReturnStmt
                if (lastItem instanceof BlockItemNode && !lastItem.children.isEmpty()) {
                    ASTnode stmtNode = lastItem.children.get(0);
                    if (stmtNode instanceof STMTnode && !stmtNode.children.isEmpty() &&
                            stmtNode.children.get(0) instanceof ReturnStmtNode &&
                            stmtNode.children.get(0).children.size()==3) {
                        hasReturn = true;
                    }
                }
            }

            if (!hasReturn) {
                TokenNode rbrace = (TokenNode) blockNode.children.get(blockNode.children.size() - 1);
                errorHandler.addError(rbrace.token.lineNumber, ErrorType.MISSING_RETURN);
            }
        }
        // 8. 退出函数作用域
        symbolTable.exitScope();
        currentFunction = null;
        return null;

    }

    private void visitFuncFParams(FuncFParamsNode node, Symbol funcSymbol) {
        for (ASTnode child : node.children) {
            if (child instanceof FuncFParamNode) {
                visitFuncFParam((FuncFParamNode) child, funcSymbol);
            }
        }
    }

    private void visitFuncFParam(FuncFParamNode node, Symbol funcSymbol) {
        IdentNode identNode = (IdentNode) node.children.get(1);
        String name = identNode.token.getValue();
        int lineNum = identNode.token.lineNumber;

        Symbol paramSymbol = null; //先声明

        if (symbolTable.checkRedefine(name)) {
            errorHandler.addError(lineNum, ErrorType.NAME_REDEFINITION);
        } else {
            boolean isArray = node.children.size() > 2;
            SymbolType type = isArray ? SymbolType.INT_ARRAY : SymbolType.INT;
            int dim = isArray ? 1 : 0;
            paramSymbol = new Symbol(name, type, lineNum);
            paramSymbol.setDimension(dim);
            symbolTable.addSymbol(paramSymbol);
            funcSymbol.addParamType(type);
        }
    }
    public SymbolType visit(MainFuncDefNode node) {
        currentFunction = new Symbol("main", SymbolType.INT_FUNC, 0); // 临时
        symbolTable.enterScope();

        BlockNode blockNode =(BlockNode) node.children.get(4);
        for (int i = 1; i < blockNode.children.size() - 1; i++) {
            ASTnode blockItem = blockNode.children.get(i);
            if (blockItem != null) {
                blockItem.accept(this); // 访问 BlockItem (Decl 或 Stmt)
            }
        }

        boolean hasReturn = false;
        // Block -> '{' {BlockItem} '}'
        if (blockNode.children.size() >= 2) {
            ASTnode lastItem = blockNode.children.get(blockNode.children.size() - 2);
            // BlockItem -> Stmt -> ReturnStmt
            if (lastItem instanceof BlockItemNode && !lastItem.children.isEmpty()) {
                ASTnode stmtNode = lastItem.children.get(0);
                if (stmtNode instanceof STMTnode && !stmtNode.children.isEmpty() &&
                        stmtNode.children.get(0) instanceof ReturnStmtNode &&
                        stmtNode.children.get(0).children.size()==3) {
                    hasReturn = true;
                }
            }
        }

        if (!hasReturn) {
            TokenNode rbrace = (TokenNode) blockNode.children.get(blockNode.children.size() - 1);
            errorHandler.addError(rbrace.token.lineNumber, ErrorType.MISSING_RETURN);
        }

        symbolTable.exitScope();
        currentFunction = null;
        return null;
    }

    public SymbolType visit(BlockNode node) {
        symbolTable.enterScope();
        for (ASTnode child : node.children) {
            if (child != null) {
                child.accept(this);
            }
        }
        symbolTable.exitScope();
        return null;
    }

    
    public SymbolType visit(AssignStmtNode node) {
        LValNode lval = (LValNode) node.children.get(0);
        IdentNode identNode = (IdentNode) lval.children.get(0);
        Symbol symbol = symbolTable.findSymbol(identNode.token.getValue());

        if (symbol != null && symbol.getType().isConst()) {
            errorHandler.addError(identNode.token.lineNumber, ErrorType.MODIFY_CONSTANT);
        }

        lval.accept(this);
        node.children.get(2).accept(this); // Visit Exp
        return null;
    }

    
    public SymbolType visit(ForLoopNode node) {
        loopDepth++;
        for (ASTnode child : node.children) {
            if (child != null) {
                child.accept(this);
            }
        }
        loopDepth--;
        return null;
    }

    
    public SymbolType visit(ForStmtNode node) {
        for (int i = 0; i < node.children.size(); i += 4) {
            LValNode lval = (LValNode) node.children.get(i);
            IdentNode identNode = (IdentNode) lval.children.get(0);
            Symbol symbol = symbolTable.findSymbol(identNode.token.getValue());
            if (symbol != null && symbol.getType().isConst()) {
                errorHandler.addError(identNode.token.lineNumber, ErrorType.MODIFY_CONSTANT);
            }

            lval.accept(this);
            if (i + 2 < node.children.size()) {
                node.children.get(i + 2).accept(this); // Visit Exp
            }
        }
        return null;
    }

    public SymbolType visit(BreakStmtNode node) {
        if (loopDepth == 0) {
            TokenNode breakToken = (TokenNode) node.children.get(0);
            errorHandler.addError(breakToken.token.lineNumber, ErrorType.BREAK_CONTINUE_OUTSIDE_LOOP);
        }
        return null;
    }

    
    public SymbolType visit(ContinueStmtNode node) {
        if (loopDepth == 0) {
            TokenNode continueToken = (TokenNode) node.children.get(0);
            errorHandler.addError(continueToken.token.lineNumber, ErrorType.BREAK_CONTINUE_OUTSIDE_LOOP);
        }
        return null;
    }

    
    public SymbolType visit(ReturnStmtNode node) {
        SymbolType funcType = (currentFunction != null) ? currentFunction.getType() : null;
        boolean hasExp = node.children.size() > 2; // 'return', Exp, ';'

        if (funcType == SymbolType.VOID_FUNC && hasExp) {
            TokenNode returnToken = (TokenNode) node.children.get(0);
            errorHandler.addError(returnToken.token.lineNumber, ErrorType.MISMATCHED_RETURN_IN_VOID_FUNCTION);
        }
        // 有return 但是 exp为空的情况，还不确定要不要写



        if (hasExp) {
            node.children.get(1).accept(this);
        }
        return null;
    }

    
    public SymbolType visit(PrintfStmtNode node) {
        TokenNode printfToken = (TokenNode) node.children.get(0);
        StringConstNode formatStringNode = (StringConstNode) node.children.get(2);
        String formatString = formatStringNode.token.getValue();

        int formatSpecifiers = 0;
        for (int i = 0; i < formatString.length() - 1; i++) {
            if (formatString.charAt(i) == '%' && formatString.charAt(i+1) == 'd') {
                formatSpecifiers++;
                i++;
            }

        }

        int expCount = 0;
        for (ASTnode child : node.children) {
            if (child instanceof EXPnode) {
                expCount++;
                child.accept(this);
            }
        }

        if (formatSpecifiers != expCount) {
            errorHandler.addError(printfToken.token.lineNumber, ErrorType.PRINTF_ARGUMENT_MISMATCH);
        }
        return null;
    }


    
    public SymbolType visit(LValNode node) {
        IdentNode identNode = (IdentNode) node.children.get(0);
        String name = identNode.token.getValue();
        int lineNum = identNode.token.lineNumber;
        Symbol symbol = symbolTable.findSymbol(name);

        if (symbol == null) {
            errorHandler.addError(lineNum, ErrorType.UNDEFINED_NAME);
            return SymbolType.INT;
        }

        boolean isArrayAccess = node.children.size() > 1; // 有 '[' Exp ']'部分

        if (isArrayAccess) {
            node.children.get(2).accept(this);
        }

        if (isArrayAccess) {
            if (!symbol.getType().isArray()) {
                errorHandler.addError(lineNum, ErrorType.UNDEFINED_NAME);
                return SymbolType.INT;
            }
            return SymbolType.INT;
        } else {
            if (symbol.getType().isArray()) {

                return symbol.getType();
            }
            return symbol.getType();
        }
    }

    
    public SymbolType visit(UnaryExpNode node) {
        ASTnode firstChild = node.children.get(0);

        //  PrimaryExp
        if (firstChild instanceof PrimaryExpNode) {
            return firstChild.accept(this);
        }

        //  UnaryOp UnaryExp
        if (firstChild instanceof UnaryOpNode) {
             return node.children.get(1).accept(this);
        }

        // Function Call -> Ident '(' [FuncRParams] ')'
        if (firstChild instanceof IdentNode identNode) {
            String funcName = identNode.token.getValue();
            int lineNum = identNode.token.lineNumber;
            Symbol funcSymbol = symbolTable.findSymbol(funcName);

            //'c' error
            if (funcSymbol == null) {
                errorHandler.addError(lineNum, ErrorType.UNDEFINED_NAME);
                return SymbolType.INT;
            }
            if (!funcSymbol.getType().isFunc()) {//会不会出现这种情况？
                errorHandler.addError(lineNum, ErrorType.UNDEFINED_NAME);
                return SymbolType.INT;
            }


            ArrayList<SymbolType> expectedParams = funcSymbol.getParamTypes();

            ArrayList<SymbolType> actualParams = new ArrayList<>();
            FuncRParamsNode rparamsNode = null;
            if (node.children.size() > 2 && node.children.get(2) instanceof FuncRParamsNode) {
                rparamsNode = (FuncRParamsNode) node.children.get(2);
                for (ASTnode expChild : rparamsNode.children) {
                    if (expChild instanceof EXPnode) {
                        actualParams.add(expChild.accept(this));
                    }
                }
            }

            // 'd' error:
            if (expectedParams.size() != actualParams.size()) {
                errorHandler.addError(lineNum, ErrorType.ARGUMENT_COUNT_MISMATCH);
            }
            //'e' error
            else {
                for (int i = 0; i < expectedParams.size(); i++) {
                    SymbolType expected = expectedParams.get(i);
                    SymbolType actual = actualParams.get(i);

                    if (actual == null) continue;
                    if (expected.isArray() != actual.isArray()) {
                        errorHandler.addError(lineNum, ErrorType.ARGUMENT_TYPE_MISMATCH);
                        break;
                    }
                }
            }

            return (funcSymbol.getType() == SymbolType.INT_FUNC) ? SymbolType.INT : SymbolType.VOID_FUNC;
        }
        return SymbolType.INT;
    }
    
    public SymbolType visit(PrimaryExpNode node) {
        ASTnode child = node.children.get(0);
        if (child instanceof TokenNode && ((TokenNode)child).token.getTokenType() == Token.TokenType.LPARENT) { // '(' Exp ')'
            return node.children.get(1).accept(this); // Return Exp's type
        } else { // LVal | Number
            return child.accept(this);
        }
    }
    public SymbolType visit(EXPnode node) {
            return node.children.get(0).accept(this);
    }

    public SymbolType visit(ConstExpNode node) {
            return node.children.get(0).accept(this);
    }

    public SymbolType visit(CondNode node) {
            return node.children.get(0).accept(this);
    }

    public SymbolType visit(AddExpNode node) {
            if(node.children.size()>1){
            return SymbolType.INT; // 没有指针运算
        } else if (!node.children.isEmpty()) {
            return node.children.get(0).accept(this);
        }
        return null;
    }

    public SymbolType visit(MulExpNode node) {
        if (node.children.size() > 1) {
            return SymbolType.INT;
        } else if (!node.children.isEmpty()) {
            return node.children.get(0).accept(this);
        }
        return null;
    }

    public SymbolType visit(RelExpNode node) {
        if (node.children.size() > 1) {
            return SymbolType.INT;
        } else if (!node.children.isEmpty()) {
            return node.children.get(0).accept(this);
        }
        return null;
    }

    public SymbolType visit(EqExpNode node) {
        if (node.children.size() > 1) {

            return SymbolType.INT;
        } else if (!node.children.isEmpty()) {
            return node.children.get(0).accept(this);
        }
        return null;
    }

    public SymbolType visit(LAndExpNode node) {
        if (node.children.size() > 1) {
            return SymbolType.INT;
        } else if (!node.children.isEmpty()) {
            return node.children.get(0).accept(this);
        }
        return null;
    }

    public SymbolType visit(LOrExpNode node) {
        if (node.children.size() > 1) {
            return SymbolType.INT;
        } else if (!node.children.isEmpty()) {
            return node.children.get(0).accept(this);
        }
        return null;
    }

    public SymbolType visit(NumberNode node) {
        return SymbolType.INT;
    }

}
