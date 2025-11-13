package frontend;

import frontend.ast.*;
import frontend.ast.exp.*;
import frontend.ast.stmt.*;
import frontend.ast.terminal.*;
import error.CompileError;
import error.MyErrorHandler;

import java.util.ArrayList;

public class Parser {
    private final MyErrorHandler errorHandler = MyErrorHandler.getInstance();
    private ArrayList<Token> tokens;
    private int currentIndex;
    private int recordIndex;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        currentIndex = 0;
        recordIndex = 0;
    }

    public void setRecordIndex() {
        this.recordIndex = currentIndex;
    }

    public void backToRecordIndex() {
        currentIndex = recordIndex;
    }

    public Token peek(int index) {
        if (currentIndex + index >= 0 && currentIndex + index < tokens.size()) {
            return tokens.get(currentIndex + index);
        }
        return null;
    }

    public Token getCurrentToken() {
        return tokens.get(currentIndex);
    }

    public Token readToken() {
        return tokens.get(currentIndex++);
    }

    public void next() {
        currentIndex++;
    }

    private boolean isAtEnd() {
        return currentIndex >= tokens.size();
    }

    public CompUnitNode parseCompUnit() {
        CompUnitNode compUnitNode = new CompUnitNode();

        while (!isAtEnd()) {
            if (peek(1).getTokenType().equals(Token.TokenType.MAINTK)) {
                compUnitNode.addNode(parseMainFuncDef());
                ;
            } else if (peek(2).getTokenType().equals(Token.TokenType.LPARENT)) {
                compUnitNode.addNode(parseFuncDef());
            } else if (getCurrentToken().getTokenType().equals(Token.TokenType.CONSTTK) ||
                    getCurrentToken().getTokenType().equals(Token.TokenType.INTTK)) {
                compUnitNode.addNode(parseDecl());
            } else {
                //出现问题
            }

        }
        return compUnitNode;
    }

    public MainFuncDefNode parseMainFuncDef() {
        MainFuncDefNode mainFuncDefNode = new MainFuncDefNode();
        mainFuncDefNode.addNode(parseToken());
        mainFuncDefNode.addNode(parseToken());
        mainFuncDefNode.addNode(parseToken());
        if (peek(0).getTokenType() == Token.TokenType.RPARENT) {
            mainFuncDefNode.addNode(parseToken()); // )
        } else {
            // 错误 j
            mainFuncDefNode.addNode(new TokenNode(new Token(Token.TokenType.RPARENT,")")));
            Token prevToken = peek(-1);
            if(prevToken != null) {
                errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_RIGHT_PARENTHESIS);
            }
        }
        mainFuncDefNode.addNode(parseBlock());
        return mainFuncDefNode;


    }

    public FuncDefNode parseFuncDef() {
        FuncDefNode funcDefNode = new FuncDefNode();
        funcDefNode.addNode(parseFuncType());
        funcDefNode.addNode(parseIdent());
        funcDefNode.addNode(parseToken());
        if (peek(0).getTokenType() == Token.TokenType.INTTK) {
            funcDefNode.addNode(parseFuncFParams());
        }
        if (getCurrentToken().getTokenType().equals(Token.TokenType.RPARENT)) {
            funcDefNode.addNode(parseToken()); // )
        } else {
            // 错误 j
            funcDefNode.addNode(new TokenNode(new Token(Token.TokenType.RPARENT,")")));
            Token prevToken = peek(-1);
            if(prevToken != null) {
                errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_RIGHT_PARENTHESIS);
            }
        }
        funcDefNode.addNode(parseBlock());
        return funcDefNode;
    }

    public DeclNode parseDecl() {
        DeclNode declNode = new DeclNode(SyntaxType.DECL);
        if (peek(0).getTokenType().equals(Token.TokenType.CONSTTK)) {
            declNode.addNode(parseConstDecl());
        } else {
            declNode.addNode(parseVarDecl());
        }
        return declNode;
    }

    public ConstDeclNode parseConstDecl() {
        ConstDeclNode constDeclNode = new ConstDeclNode();
        constDeclNode.addNode(parseToken());
        constDeclNode.addNode(parseBType());
        constDeclNode.addNode(parseConstDef());
        while (peek(0).getTokenType().equals(Token.TokenType.COMMA)) {
            constDeclNode.addNode(parseToken());
            constDeclNode.addNode(parseConstDef());
        }
        if (peek(0).getTokenType().equals(Token.TokenType.SEMICN)) {
            constDeclNode.addNode(parseToken());
        } else {
            constDeclNode.addNode(new TokenNode(new Token(Token.TokenType.SEMICN,";")));
            Token prevToken = peek(-1);
            if (prevToken != null) {
                errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_SEMICOLON);
            }
        }
        return constDeclNode;
    }

    public ConstDefNode parseConstDef() {
        ConstDefNode constDefNode = new ConstDefNode();
        constDefNode.addNode(parseIdent());
        if (peek(0).getTokenType().equals(Token.TokenType.LBRACK)) {
            constDefNode.addNode(parseToken());
            constDefNode.addNode(parseConstExp());

            if (peek(0).getTokenType().equals(Token.TokenType.RBRACK)) {
                constDefNode.addNode(parseToken()); // ]
            } else {
                // 错误 k
                constDefNode.addNode(new TokenNode(new Token(Token.TokenType.RBRACK,"]")));
                Token prevToken = peek(-1);
                if(prevToken != null) {
                    errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_RIGHT_BRACKET);
                }
            }

        }
        constDefNode.addNode(parseToken());
        constDefNode.addNode(parseConstInitVal());
        return constDefNode;
    }

    public ConstInitValNode parseConstInitVal() {
        ConstInitValNode constInitValNode = new ConstInitValNode();
        if (peek(0).getTokenType().equals(Token.TokenType.LBRACE)) {
            constInitValNode.addNode(parseToken());
            if (peek(0).getTokenType().equals(Token.TokenType.RBRACE)) {
                constInitValNode.addNode(parseToken());
                return constInitValNode;
            }
            constInitValNode.addNode(parseConstExp());
            while (peek(0).getTokenType().equals(Token.TokenType.COMMA)) {
                constInitValNode.addNode(parseToken());
                constInitValNode.addNode(parseConstExp());
            }
            constInitValNode.addNode(parseToken());
        } else {
            constInitValNode.addNode(parseConstExp());
        }
        return constInitValNode;
    }

    public ConstExpNode parseConstExp() {
        ConstExpNode constExpNode = new ConstExpNode();
        constExpNode.addNode(parseAddExp());
        return constExpNode;
    }


    public VarDeclNode parseVarDecl() {
        VarDeclNode varDeclNode = new VarDeclNode();
        if (peek(0).getTokenType().equals(Token.TokenType.STATICTK)) {
            varDeclNode.addNode(parseToken());
        }
        varDeclNode.addNode(parseBType());
        varDeclNode.addNode(parseVarDef());
        while (peek(0).getTokenType().equals(Token.TokenType.COMMA)) {
            varDeclNode.addNode(parseToken());
            varDeclNode.addNode(parseVarDef());
        }
        if (peek(0).getTokenType().equals(Token.TokenType.SEMICN)) {
            varDeclNode.addNode(parseToken());
        } else {
            // 错误 i: 缺少分号
            varDeclNode.addNode(new TokenNode(new Token(Token.TokenType.SEMICN,";")));
            Token prevToken = peek(-1);
            if(prevToken != null) {
                errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_SEMICOLON);
            }
        }
        return varDeclNode;
    }

    public VarDefNode parseVarDef() {
        VarDefNode varDefNode = new VarDefNode();
        varDefNode.addNode(parseIdent());
        if (peek(0).getTokenType().equals(Token.TokenType.LBRACK)) {
            varDefNode.addNode(parseToken());
            varDefNode.addNode(parseConstExp());
            if (peek(0).getTokenType().equals(Token.TokenType.RBRACK)) {
                varDefNode.addNode(parseToken()); // ]
            } else {
                // 错误 k
                varDefNode.addNode(new TokenNode(new Token(Token.TokenType.RBRACK,"]")));
                Token prevToken = peek(-1);
                if(prevToken != null) {
                    errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_RIGHT_BRACKET);
                }
            }
        }
        if (peek(0).getTokenType().equals(Token.TokenType.ASSIGN)) {
            varDefNode.addNode(parseToken());
            varDefNode.addNode(parseInitVal());
        }
        return varDefNode;
    }

    public InitValNode parseInitVal() {
        InitValNode initValNode = new InitValNode();
        if (peek(0).getTokenType().equals(Token.TokenType.LBRACE)) {
            initValNode.addNode(parseToken());
            if (peek(0).getTokenType().equals(Token.TokenType.RBRACE)) {
                initValNode.addNode(parseToken());
                return initValNode;
            }
            initValNode.addNode(parseExp());
            while (peek(0).getTokenType().equals(Token.TokenType.COMMA)) {
                initValNode.addNode(parseToken());
                initValNode.addNode(parseExp());
            }
            initValNode.addNode(parseToken());
        } else {
            initValNode.addNode(parseExp());
        }
        return initValNode;
    }

    public EXPnode parseExp() {
        EXPnode expNode = new EXPnode(SyntaxType.EXP);
        expNode.addNode(parseAddExp());
        return expNode;
    }

    public FuncTypeNode parseFuncType() {
        FuncTypeNode funcTypeNode = new FuncTypeNode();
        funcTypeNode.addNode(new TokenNode(readToken()));
        return funcTypeNode;
    }

    public IdentNode parseIdent() {
        return new IdentNode(readToken());
        //完成
    }

    public TokenNode parseToken() {
        return new TokenNode(readToken());
        //完成
    }

    public BlockNode parseBlock() {
        BlockNode blockNode = new BlockNode();
        blockNode.addNode(parseToken());
        while (!peek(0).getTokenType().equals(Token.TokenType.RBRACE)) {
            blockNode.addNode(parseBlockItem());
        }
        blockNode.addNode(parseToken());
        return blockNode;
    }

    public FuncFParamNode parseFuncFParam() {
        FuncFParamNode funcFParamNode = new FuncFParamNode();
        funcFParamNode.addNode(parseBType());
        funcFParamNode.addNode(parseIdent());
        if (peek(0).getTokenType().equals(Token.TokenType.LBRACK)) {
            funcFParamNode.addNode(parseToken()); // [
            if (peek(0).getTokenType().equals(Token.TokenType.RBRACK)) {
                funcFParamNode.addNode(parseToken()); // ]
            } else {
                // 错误 k
                funcFParamNode.addNode(new TokenNode(new Token(Token.TokenType.RBRACK,"]")));
                Token prevToken = peek(-1);
                if(prevToken != null) {
                    errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_RIGHT_BRACKET);
                }
            }
        }
        return funcFParamNode;
    }

    public FuncFParamsNode parseFuncFParams() {
        FuncFParamsNode funcFParamsNode = new FuncFParamsNode();
        funcFParamsNode.addNode(parseFuncFParam());
        while (peek(0).getTokenType().equals(Token.TokenType.COMMA)) {
            funcFParamsNode.addNode(parseToken());
            funcFParamsNode.addNode(parseFuncFParam());
        }

        return funcFParamsNode;

    }

    public BTypeNode parseBType() {
        return new BTypeNode(readToken());
    }

    public BlockItemNode parseBlockItem() {
        BlockItemNode blockItemNode = new BlockItemNode();
        if (peek(0).getTokenType().equals(Token.TokenType.CONSTTK) ||
                peek(0).getTokenType().equals(Token.TokenType.INTTK) ||
                peek(0).getTokenType().equals(Token.TokenType.STATICTK)) {
            blockItemNode.addNode(parseDecl());
        } else {
            blockItemNode.addNode(parseStmt());
        }
        return blockItemNode;
    }

    public STMTnode parseStmt() {
        STMTnode stmtNode = new STMTnode(SyntaxType.STMT);
        Token token = peek(0);
        switch (token.getTokenType()) {
            case IFTK -> stmtNode.addNode(parseIfStmt());
            case FORTK -> stmtNode.addNode(parseForLoop());
            case BREAKTK -> stmtNode.addNode(parseBreakStmt());
            case CONTINUETK -> stmtNode.addNode(parseContinueStmt());
            case RETURNTK -> stmtNode.addNode(parseReturnStmt());
            case PRINTFTK -> stmtNode.addNode(parsePrintfStmt());
            case LBRACE -> stmtNode.addNode(parseBlock());
            case SEMICN -> stmtNode.addNode(parseExpStmt());
            default -> {
                setRecordIndex();
                errorHandler.stopAnalysis();
                parseLval();
                boolean isAssignment = !isAtEnd() && peek(0) != null && peek(0).getTokenType() == Token.TokenType.ASSIGN;
                backToRecordIndex();
                errorHandler.beginAnalysis();
                if (isAssignment) {
                    AssignStmtNode assignNode = new AssignStmtNode();
                    assignNode.addNode(parseLval());
                    assignNode.addNode(parseToken()); // 解析 '='
                    assignNode.addNode(parseExp());
                    stmtNode.addNode(assignNode);

                    if (!isAtEnd() && peek(0) != null && peek(0).getTokenType() == Token.TokenType.SEMICN) {
                        assignNode.addNode(parseToken());
                    } else {
                        assignNode.addNode(new TokenNode(new Token(Token.TokenType.SEMICN,";")));
                        Token prevToken = peek(-1);
                        if (prevToken != null) {
                            errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_SEMICOLON);
                        }
                    }
                } else {
                    stmtNode.addNode(parseExpStmt());
                }
            }
        }
        return stmtNode;

    }

    public IfStmtNode parseIfStmt() {
        IfStmtNode ifStmtNode = new IfStmtNode();
        ifStmtNode.addNode(parseToken());
        ifStmtNode.addNode(parseToken());
        ifStmtNode.addNode(parseCond());
        if (peek(0).getTokenType().equals(Token.TokenType.RPARENT)) {
            ifStmtNode.addNode(parseToken()); // )
        } else {
            // 错误 j
            ifStmtNode.addNode(new TokenNode(new Token(Token.TokenType.RPARENT,")")));
            Token prevToken = peek(-1);
            if(prevToken != null) {
                errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_RIGHT_PARENTHESIS);
            }
        }
        ifStmtNode.addNode(parseStmt());
        if (peek(0).getTokenType().equals(Token.TokenType.ELSETK)) {
            ifStmtNode.addNode(parseToken());
            ifStmtNode.addNode(parseStmt());
        }

        return ifStmtNode;
    }

    public CondNode parseCond() {
        CondNode condNode = new CondNode();
        condNode.addNode(parseLOrExp());
        return condNode;
    }

    public ForLoopNode parseForLoop() {
        ForLoopNode forLoopNode = new ForLoopNode();
        forLoopNode.addNode(parseToken());
        forLoopNode.addNode(parseToken());
        if (!peek(0).getTokenType().equals(Token.TokenType.SEMICN)) {
            forLoopNode.addNode(parseForStmt());
        }
        forLoopNode.addNode(parseToken());
        if (!peek(0).getTokenType().equals(Token.TokenType.SEMICN)) {
            forLoopNode.addNode(parseCond());
        }
        forLoopNode.addNode(parseToken());
        if (peek(0).getTokenType().equals(Token.TokenType.IDENFR)) {
            forLoopNode.addNode(parseForStmt());
        }
        if (peek(0) != null && peek(0).getTokenType() == Token.TokenType.RPARENT) {
            forLoopNode.addNode(parseToken());
        } else {
            // Error j:
            forLoopNode.addNode(new TokenNode(new Token(Token.TokenType.RPARENT,")")));
            Token prevToken = peek(-1);
            if (prevToken != null) {
                errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_RIGHT_PARENTHESIS);
            }
        }
        forLoopNode.addNode(parseStmt());
        return forLoopNode;


    }

    public BreakStmtNode parseBreakStmt() {
        BreakStmtNode breakStmtNode = new BreakStmtNode();
        breakStmtNode.addNode(parseToken());
        if (peek(0).getTokenType() == Token.TokenType.SEMICN) {
            breakStmtNode.addNode(parseToken());
        } else {
            breakStmtNode.addNode(new TokenNode(new Token(Token.TokenType.SEMICN,";")));
            Token prevToken = peek(-1);
            if(prevToken != null) {
                errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_SEMICOLON);
            }
        }
        return breakStmtNode;
    }

    public ContinueStmtNode parseContinueStmt() {
        ContinueStmtNode continueStmtNode = new ContinueStmtNode();
        continueStmtNode.addNode(parseToken());
        if (peek(0).getTokenType() == Token.TokenType.SEMICN) {
            continueStmtNode.addNode(parseToken());
        } else {
            continueStmtNode.addNode(new TokenNode(new Token(Token.TokenType.SEMICN,";")));
            Token prevToken = peek(-1);
            if(prevToken != null) {
                errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_SEMICOLON);
            }
        }
        return continueStmtNode;
    }

    public ReturnStmtNode parseReturnStmt() {
        ReturnStmtNode returnStmtNode = new ReturnStmtNode();
        returnStmtNode.addNode(parseToken());
        if (!peek(0).getTokenType().equals(Token.TokenType.SEMICN)) {
            returnStmtNode.addNode(parseExp());
        }
        if (peek(0).getTokenType() == Token.TokenType.SEMICN) {
            returnStmtNode.addNode(parseToken());
        } else {
            returnStmtNode.addNode(new TokenNode(new Token(Token.TokenType.SEMICN,";")));
            Token prevToken = peek(-1);
            if(prevToken != null) {
                errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_SEMICOLON);
            }
        }
        return returnStmtNode;
    }

    public PrintfStmtNode parsePrintfStmt() {
        PrintfStmtNode printfStmtNode = new PrintfStmtNode();
        printfStmtNode.addNode(parseToken()); // printf
        printfStmtNode.addNode(parseToken()); // (
        printfStmtNode.addNode(parseStringConst());
        while (peek(0).getTokenType().equals(Token.TokenType.COMMA)) {
            printfStmtNode.addNode(parseToken());
            printfStmtNode.addNode(parseExp());
        }
        // 检查右小括号 ')'
        if (peek(0).getTokenType() == Token.TokenType.RPARENT) {
            printfStmtNode.addNode(parseToken());
        } else {
            // 错误 j: 缺少右小括号
            printfStmtNode.addNode(new TokenNode(new Token(Token.TokenType.RPARENT,")")));
            Token prevToken = peek(-1);
            if(prevToken != null) {
                errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_RIGHT_PARENTHESIS);
            }
        }
        // 检查分号 ';'
        if (peek(0).getTokenType() == Token.TokenType.SEMICN) {
            printfStmtNode.addNode(parseToken());
        } else {
            // 错误 i: 缺少分号
            printfStmtNode.addNode(new TokenNode(new Token(Token.TokenType.SEMICN,";")));
            Token prevToken = peek(-1);
            if(prevToken != null) {
                errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_SEMICOLON);
            }
        }
        return printfStmtNode;
    }

    public StringConstNode parseStringConst() {
        return new StringConstNode(readToken());
    }

    public ExpStmtNode parseExpStmt() {
        ExpStmtNode expStmtNode = new ExpStmtNode();
        if (!peek(0).getTokenType().equals(Token.TokenType.SEMICN)) {
            expStmtNode.addNode(parseExp());
        }
        if (peek(0).getTokenType().equals(Token.TokenType.SEMICN)) {
            expStmtNode.addNode(parseToken()); // 消耗分号
        } else {
            // 错误 i: 缺少分号
            expStmtNode.addNode(new TokenNode(new Token(Token.TokenType.SEMICN,";")));
            Token prevToken = peek(-1);
            if (prevToken != null) {
                errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_SEMICOLON);
            }
        }
        return expStmtNode;
    }

    public ForStmtNode parseForStmt() {
        ForStmtNode forStmtNode = new ForStmtNode();
        forStmtNode.addNode(parseLval());
        forStmtNode.addNode(parseToken());
        forStmtNode.addNode(parseExp());
        while (peek(0).getTokenType().equals(Token.TokenType.COMMA)) {
            forStmtNode.addNode(parseToken());
            forStmtNode.addNode(parseLval());
            forStmtNode.addNode(parseToken());
            forStmtNode.addNode(parseExp());
        }
        return forStmtNode;
    }

    public LValNode parseLval() {
        LValNode lvalNode = new LValNode();
        lvalNode.addNode(parseIdent());
        if (peek(0).getTokenType().equals(Token.TokenType.LBRACK)) {
            lvalNode.addNode(parseToken()); // [
            lvalNode.addNode(parseExp());
            if (peek(0).getTokenType().equals(Token.TokenType.RBRACK)) {
                lvalNode.addNode(parseToken()); // ]
            } else {
                // 错误 k
                lvalNode.addNode(new TokenNode(new Token(Token.TokenType.RBRACK,"]")));
                Token prevToken = peek(-1);
                if(prevToken != null) {
                    errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_RIGHT_BRACKET);
                }
            }
        }
        return lvalNode;
    }


    public LOrExpNode parseLOrExp() {
        LAndExpNode lAndExpNode = parseLAndExp();
        LOrExpNode lOrExpNode = new LOrExpNode();
        lOrExpNode.addNode(lAndExpNode);

        while (peek(0).getTokenType().equals(Token.TokenType.OR)) {
            LOrExpNode newRoot = new LOrExpNode();
            newRoot.addNode(lOrExpNode);
            newRoot.addNode(parseToken());
            newRoot.addNode(parseLAndExp());
            lOrExpNode = newRoot;
        }
        return lOrExpNode;
    }


    public LAndExpNode parseLAndExp() {
        EqExpNode eqExpNode = parseEqExp();

        LAndExpNode lAndExpNode = new LAndExpNode();
        lAndExpNode.addNode(eqExpNode);

        while (peek(0).getTokenType().equals(Token.TokenType.AND)) {
            LAndExpNode newRoot = new LAndExpNode();
            newRoot.addNode(lAndExpNode);
            newRoot.addNode(parseToken());
            newRoot.addNode(parseEqExp());
            lAndExpNode = newRoot;
        }
        return lAndExpNode;
    }


    public EqExpNode parseEqExp() {
        RelExpNode relExpNode = parseRelExp();

        EqExpNode eqExpNode = new EqExpNode();
        eqExpNode.addNode(relExpNode);

        while (peek(0).getTokenType().equals(Token.TokenType.EQL) ||
                peek(0).getTokenType().equals(Token.TokenType.NEQ)) {
            EqExpNode newRoot = new EqExpNode();
            newRoot.addNode(eqExpNode);
            newRoot.addNode(parseToken());
            newRoot.addNode(parseRelExp());
            eqExpNode = newRoot;
        }
        return eqExpNode;
    }


    public RelExpNode parseRelExp() {
        AddExpNode addExpNode = parseAddExp();

        RelExpNode relExpNode = new RelExpNode();
        relExpNode.addNode(addExpNode);

        while (peek(0).getTokenType().equals(Token.TokenType.LSS) ||
                peek(0).getTokenType().equals(Token.TokenType.GRE) ||
                peek(0).getTokenType().equals(Token.TokenType.LEQ) ||
                peek(0).getTokenType().equals(Token.TokenType.GEQ)) {
            RelExpNode newRoot = new RelExpNode();
            newRoot.addNode(relExpNode);
            newRoot.addNode(parseToken());
            newRoot.addNode(parseAddExp());
            relExpNode = newRoot;
        }
        return relExpNode;
    }


    public AddExpNode parseAddExp() {
        MulExpNode mulExpNode = parseMulExp();

        AddExpNode addExpNode = new AddExpNode();
        addExpNode.addNode(mulExpNode);

        while (peek(0).getTokenType().equals(Token.TokenType.PLUS) ||
                peek(0).getTokenType().equals(Token.TokenType.MINU)) {
            AddExpNode newRoot = new AddExpNode();
            newRoot.addNode(addExpNode);
            newRoot.addNode(parseToken());
            newRoot.addNode(parseMulExp());
            addExpNode = newRoot;
        }
        return addExpNode;
    }


    public MulExpNode parseMulExp() {
        UnaryExpNode unaryExpNode = parseUnaryExp();

        MulExpNode mulExpNode = new MulExpNode();
        mulExpNode.addNode(unaryExpNode);

        while (peek(0).getTokenType().equals(Token.TokenType.MULT) ||
                peek(0).getTokenType().equals(Token.TokenType.DIV) ||
                peek(0).getTokenType().equals(Token.TokenType.MOD)) {
            MulExpNode newRoot = new MulExpNode();
            newRoot.addNode(mulExpNode);
            newRoot.addNode(parseToken());
            newRoot.addNode(parseUnaryExp());
            mulExpNode = newRoot;
        }
        return mulExpNode;
    }


    public UnaryExpNode parseUnaryExp() {
        UnaryExpNode unaryExpNode = new UnaryExpNode();
        Token token = peek(0);
        if (token.getTokenType().equals(Token.TokenType.PLUS) ||
                token.getTokenType().equals(Token.TokenType.MINU) ||
                token.getTokenType().equals(Token.TokenType.NOT)) {
            unaryExpNode.addNode(parseUnaryOp());
            unaryExpNode.addNode(parseUnaryExp());
        } else if (token.getTokenType().equals(Token.TokenType.IDENFR) &&
                peek(1).getTokenType().equals(Token.TokenType.LPARENT)) {
            unaryExpNode.addNode(parseIdent());
            unaryExpNode.addNode(parseToken()); // '('
            if (peek(0).getTokenType().equals(Token.TokenType.INTCON) ||
                    peek(0).getTokenType().equals(Token.TokenType.IDENFR) ||
                    peek(0).getTokenType().equals(Token.TokenType.PLUS) ||
                    peek(0).getTokenType().equals(Token.TokenType.MINU) ||
                    peek(0).getTokenType().equals(Token.TokenType.NOT) ||
                    peek(0).getTokenType().equals(Token.TokenType.LPARENT)) {
                unaryExpNode.addNode(parseFuncRParams());
            }
            if (peek(0).getTokenType().equals(Token.TokenType.RPARENT)) {
                unaryExpNode.addNode(parseToken()); // ')'
            } else {
                // 错误 j
                unaryExpNode.addNode(new TokenNode(new Token(Token.TokenType.RPARENT,")")));
                Token prevToken = peek(-1);
                if(prevToken != null) {
                    errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_RIGHT_PARENTHESIS);
                }
            }
        } else {
            unaryExpNode.addNode(parsePrimaryExp());
        }
        return unaryExpNode;
    }


    public UnaryOpNode parseUnaryOp() {
        UnaryOpNode unaryOpNode = new UnaryOpNode();
        unaryOpNode.addNode(parseToken());
        return unaryOpNode;
    }


    public PrimaryExpNode parsePrimaryExp() {
        PrimaryExpNode primaryExpNode = new PrimaryExpNode();
        if (peek(0).getTokenType().equals(Token.TokenType.LPARENT)) {
            primaryExpNode.addNode(parseToken()); // '('
            primaryExpNode.addNode(parseExp());

            if (peek(0).getTokenType().equals(Token.TokenType.RPARENT)) {
                primaryExpNode.addNode(parseToken()); // ')'
            } else {
                // 错误 j
                primaryExpNode.addNode(new TokenNode(new Token(Token.TokenType.RPARENT,")")));
                Token prevToken = peek(-1);
                if(prevToken != null) {
                    errorHandler.addError(prevToken.lineNumber, CompileError.ErrorType.MISSING_RIGHT_PARENTHESIS);
                }
            }
        } else if (peek(0).getTokenType().equals(Token.TokenType.INTCON)) {
            primaryExpNode.addNode(parseNumber());
        } else {
            primaryExpNode.addNode(parseLval());
        }
        return primaryExpNode;
    }


    public FuncRParamsNode parseFuncRParams() {
        FuncRParamsNode funcRParamsNode = new FuncRParamsNode();
        funcRParamsNode.addNode(parseExp());
        while (peek(0).getTokenType().equals(Token.TokenType.COMMA)) {
            funcRParamsNode.addNode(parseToken()); // ','
            funcRParamsNode.addNode(parseExp());
        }
        return funcRParamsNode;
    }

    public NumberNode parseNumber() {
        NumberNode numberNode = new NumberNode();
        numberNode.addNode(parseToken()); // IntConst
        return numberNode;
    }
}
