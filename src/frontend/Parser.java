package frontend;

import frontend.ast.*;
import frontend.ast.exp.*;
import frontend.ast.stmt.*;
import frontend.ast.terminal.*;

import java.util.ArrayList;

public class Parser {
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

    public Token peek(int index){
        return tokens.get(currentIndex + index);
    }

    public Token getCurrentToken(){
        return tokens.get(currentIndex);
    }

    public Token readToken(){
        return tokens.get(currentIndex++);
    }

    public void next(){
        currentIndex++;
    }

    private boolean isAtEnd() {
        return currentIndex >= tokens.size();
    }

    public CompUnitNode parseCompUnit() {
            CompUnitNode compUnitNode = new CompUnitNode();

        while (!isAtEnd()) {
            if(peek(1).getTokenType().equals(Token.TokenType.MAINTK)){
               compUnitNode.addNode(parseMainFuncDef()); ;
            }else if(peek(2).getTokenType().equals(Token.TokenType.LPARENT)){
                compUnitNode.addNode(parseFuncDef());
            }
            else if(getCurrentToken().getTokenType().equals(Token.TokenType.CONSTTK)||
                    getCurrentToken().getTokenType().equals(Token.TokenType.INTTK)){
                compUnitNode.addNode(parseDecl());
            }else{
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
        mainFuncDefNode.addNode(parseToken());
        mainFuncDefNode.addNode(parseBlock());
        return mainFuncDefNode;


    }
    public FuncDefNode parseFuncDef() {
    FuncDefNode funcDefNode = new FuncDefNode();
    funcDefNode.addNode(parseFuncType());
    funcDefNode.addNode(parseIdent());
    funcDefNode.addNode(parseToken());
    if(getCurrentToken().getTokenType().equals(Token.TokenType.RPARENT)){
        funcDefNode.addNode(parseToken());
        funcDefNode.addNode(parseBlock());
    }else {
        funcDefNode.addNode(parseFuncFParams());
        funcDefNode.addNode(parseToken());
        funcDefNode.addNode(parseBlock());
    }
    return funcDefNode;
    }
    public DECLnode parseDecl() {
    DECLnode declNode = new DECLnode(SyntaxType.DECL);
    if(peek(0).getTokenType().equals(Token.TokenType.CONSTTK)){
        declNode.addNode(parseConstDecl());
    }else {
        declNode.addNode(parseVarDecl());
    }
    return declNode;
    }

    public ConstDeclNode parseConstDecl() {
        ConstDeclNode constDeclNode = new ConstDeclNode();
        constDeclNode.addNode(parseToken());
        constDeclNode.addNode(parseBType());
        constDeclNode.addNode(parseConstDef());
        while (peek(0).getTokenType().equals(Token.TokenType.COMMA)){
            constDeclNode.addNode(parseToken());
            constDeclNode.addNode(parseConstDef());
        }
        constDeclNode.addNode(parseToken());
        return constDeclNode;
    }
    public ConstDefNode parseConstDef() {
     ConstDefNode constDefNode = new ConstDefNode();
     constDefNode.addNode(parseIdent());
     if(peek(0).getTokenType().equals(Token.TokenType.LBRACK)){
         constDefNode.addNode(parseToken());
         constDefNode.addNode(parseConstExp());
         constDefNode.addNode(parseToken());

     }constDefNode.addNode(parseToken());
        constDefNode.addNode(parseConstInitVal());
        return constDefNode;
    }

    public ConstInitValNode parseConstInitVal(){
        ConstInitValNode constInitValNode = new ConstInitValNode();
        if(peek(0).getTokenType().equals(Token.TokenType.LBRACE)){
            constInitValNode.addNode(parseToken());
            if(peek(0).getTokenType().equals(Token.TokenType.RBRACE)){
                    constInitValNode.addNode(parseToken());
                    return constInitValNode;
            }
            constInitValNode.addNode(parseConstExp());
            while (peek(0).getTokenType().equals(Token.TokenType.COMMA)){
                constInitValNode.addNode(parseToken());
                constInitValNode.addNode(parseConstExp());
            }
            constInitValNode.addNode(parseToken());
            return constInitValNode;
        }else {
            constInitValNode.addNode(parseConstExp());
            return constInitValNode;
        }
    }

    public ConstExpNode parseConstExp() {
        ConstExpNode constExpNode = new ConstExpNode();
        constExpNode.addNode(parseAddExp());
        return constExpNode;
    }


    public VarDeclNode parseVarDecl() {
        VarDeclNode varDeclNode = new VarDeclNode();
    if(peek(0).getTokenType().equals(Token.TokenType.CONSTTK)){
        varDeclNode.addNode(parseToken());
    }
    varDeclNode.addNode(parseBType());
    varDeclNode.addNode(parseVarDef());
    while (peek(0).getTokenType().equals(Token.TokenType.COMMA)){
        varDeclNode.addNode(parseToken());
        varDeclNode.addNode(parseVarDef());
    }
    varDeclNode.addNode(parseToken());
    return varDeclNode;
    }

    public VarDefNode parseVarDef() {
        VarDefNode varDefNode = new VarDefNode();
        varDefNode.addNode(parseIdent());

            varDefNode.addNode(parseToken());
            varDefNode.addNode(parseConstExp());
            varDefNode.addNode(parseToken());
        if(peek(0).getTokenType().equals(Token.TokenType.ASSIGN)){
            varDefNode.addNode(parseToken());
            varDefNode.addNode(parseInitVal());
        }
        return varDefNode;
    }

    public InitValNode parseInitVal() {
        InitValNode initValNode = new InitValNode();
        if(peek(0).getTokenType().equals(Token.TokenType.LBRACE)){
            initValNode.addNode(parseToken());
            if(peek(0).getTokenType().equals(Token.TokenType.RBRACE)){
                initValNode.addNode(parseToken());
                return initValNode;
            }
            initValNode.addNode(parseExp());
            while (peek(0).getTokenType().equals(Token.TokenType.COMMA)){
                initValNode.addNode(parseToken());
                initValNode.addNode(parseExp());
            }
            initValNode.addNode(parseToken());
            return initValNode;
        }else {
            initValNode.addNode(parseExp());
            return initValNode;
        }
    }

    public EXPnode parseExp() {
        EXPnode expNode =new EXPnode(SyntaxType.EXP);
        expNode.addNode(parseAddExp());
        return expNode;
    }

    public FuncTypeNode parseFuncType() {
        return new FuncTypeNode(readToken());
        //完成
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
        if(peek(0).getTokenType().equals(Token.TokenType.LBRACK)&&
        peek(1).getTokenType().equals(Token.TokenType.RBRACK)){
            funcFParamNode.addNode(parseToken());
            funcFParamNode.addNode(parseToken());
        }
        return funcFParamNode;
    }

    public FuncFParamsNode parseFuncFParams() {
        FuncFParamsNode funcFParamsNode = new FuncFParamsNode();

        return funcFParamsNode;
    }

    public BTypeNode parseBType() {
        return new BTypeNode(readToken());
    }

    public BlockItemNode parseBlockItem(){
        BlockItemNode blockItemNode = new BlockItemNode();
        if(peek(0).getTokenType().equals(Token.TokenType.CONSTTK)||
        peek(0).getTokenType().equals(Token.TokenType.INTTK)||
        peek(0).getTokenType().equals(Token.TokenType.STATICTK)){
            blockItemNode.addNode(parseDecl());
        }else {
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
            //区分第一条和第二条
            setRecordIndex();
            EXPnode expNode = parseExp();
            if(peek(0).getTokenType().equals(Token.TokenType.SEMICN)){
                stmtNode.addNode(expNode);
                stmtNode.addNode(parseToken());
            }else {
                backToRecordIndex();
                stmtNode.addNode(parseLval());
                stmtNode.addNode(parseToken());
                stmtNode.addNode(parseExp());
                stmtNode.addNode(parseToken());

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
        ifStmtNode.addNode(parseToken());
        ifStmtNode.addNode(parseStmt());
        if(peek(0).getTokenType().equals(Token.TokenType.ELSETK)){
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
        if(!peek(0).getTokenType().equals(Token.TokenType.SEMICN)){
            forLoopNode.addNode(parseForStmt());
        }
        forLoopNode.addNode(parseToken());
        if(!peek(0).getTokenType().equals(Token.TokenType.SEMICN)){
            forLoopNode.addNode(parseCond());
        }
        forLoopNode.addNode(parseToken());
        if(!peek(0).getTokenType().equals(Token.TokenType.RPARENT)){
            forLoopNode.addNode(parseForStmt());
        }
        forLoopNode.addNode(parseToken());
        forLoopNode.addNode(parseStmt());
        return forLoopNode;


    }

    public BreakStmtNode parseBreakStmt() {
        BreakStmtNode breakStmtNode = new BreakStmtNode();
        breakStmtNode.addNode(parseToken());
        breakStmtNode.addNode(parseToken());
        return breakStmtNode;
    }

    public ContinueStmtNode parseContinueStmt() {
        ContinueStmtNode continueStmtNode = new ContinueStmtNode();
        continueStmtNode.addNode(parseToken());
        continueStmtNode.addNode(parseToken());
        return continueStmtNode;
    }

    public ReturnStmtNode parseReturnStmt() {
        ReturnStmtNode returnStmtNode = new ReturnStmtNode();
        returnStmtNode.addNode(parseToken());
        returnStmtNode.addNode(parseExpStmt());
        return returnStmtNode;
    }

    public PrintfStmtNode parsePrintfStmt() {
        PrintfStmtNode printfStmtNode = new PrintfStmtNode();
        printfStmtNode.addNode(parseToken());
        printfStmtNode.addNode(parseToken());
        printfStmtNode.addNode(parseStringConst());
        while (peek(0).getTokenType().equals(Token.TokenType.COMMA)){
            printfStmtNode.addNode(parseToken());
            printfStmtNode.addNode(parseExp());
        }
        printfStmtNode.addNode(parseToken());
        printfStmtNode.addNode(parseToken());
        return printfStmtNode;
    }

    public StringConstNode parseStringConst() {
        return new StringConstNode(readToken());
    }
    public ExpStmtNode parseExpStmt(){
        ExpStmtNode expStmtNode = new ExpStmtNode();
        if(!peek(0).getTokenType().equals(Token.TokenType.SEMICN)){
            expStmtNode.addNode(parseExp());
        }
        expStmtNode.addNode(parseToken());
        return expStmtNode;
    }

    public ForStmtNode parseForStmt(){
        ForStmtNode forStmtNode = new ForStmtNode();
        forStmtNode.addNode(parseLval());
        forStmtNode.addNode(parseToken());
        forStmtNode.addNode(parseExp());
        while (peek(0).getTokenType().equals(Token.TokenType.COMMA)){
            forStmtNode.addNode(parseToken());
            forStmtNode.addNode(parseLval());
            forStmtNode.addNode(parseToken());
            forStmtNode.addNode(parseExp());
        }
        return forStmtNode;
    }

    public LValNode parseLval(){
        LValNode lvalNode = new LValNode();
        lvalNode.addNode(parseIdent());
        if(peek(0).getTokenType().equals(Token.TokenType.LBRACK)){
            lvalNode.addNode(parseToken());
            lvalNode.addNode(parseExp());
            lvalNode.addNode(parseToken());
        }
        return lvalNode;
    }


    public LOrExpNode parseLOrExp() {
        LOrExpNode lOrExpNode = new LOrExpNode();
        lOrExpNode.addNode(parseLAndExp());
        while (peek(0).getTokenType().equals(Token.TokenType.OR)) {
            lOrExpNode.addNode(parseToken());
            lOrExpNode.addNode(parseLAndExp());
        }
        return lOrExpNode;
    }

    public LAndExpNode parseLAndExp() {
        LAndExpNode lAndExpNode = new LAndExpNode();
        lAndExpNode.addNode(parseEqExp());
        while (peek(0).getTokenType().equals(Token.TokenType.AND)) {
            lAndExpNode.addNode(parseToken());
            lAndExpNode.addNode(parseEqExp());
        }
        return lAndExpNode;
    }


    public EqExpNode parseEqExp() {
        EqExpNode eqExpNode = new EqExpNode();
        eqExpNode.addNode(parseRelExp());
        while (peek(0).getTokenType().equals(Token.TokenType.EQL) ||
                peek(0).getTokenType().equals(Token.TokenType.NEQ)) {
            eqExpNode.addNode(parseToken());
            eqExpNode.addNode(parseRelExp());
        }
        return eqExpNode;
    }


    public RelExpNode parseRelExp() {
        RelExpNode relExpNode = new RelExpNode();
        relExpNode.addNode(parseAddExp());
        while (peek(0).getTokenType().equals(Token.TokenType.LSS) ||
                peek(0).getTokenType().equals(Token.TokenType.GRE) ||
                peek(0).getTokenType().equals(Token.TokenType.LEQ) ||
                peek(0).getTokenType().equals(Token.TokenType.GEQ)) {
            relExpNode.addNode(parseToken());
            relExpNode.addNode(parseAddExp());
        }
        return relExpNode;
    }


    public AddExpNode parseAddExp() {
        AddExpNode addExpNode = new AddExpNode();
        addExpNode.addNode(parseMulExp());
        while (peek(0).getTokenType().equals(Token.TokenType.PLUS) ||
                peek(0).getTokenType().equals(Token.TokenType.MINU)) {
            addExpNode.addNode(parseToken());
            addExpNode.addNode(parseMulExp());
        }
        return addExpNode;
    }


    public MulExpNode parseMulExp() {
        MulExpNode mulExpNode = new MulExpNode();
        mulExpNode.addNode(parseUnaryExp());
        while (peek(0).getTokenType().equals(Token.TokenType.MULT) ||
                peek(0).getTokenType().equals(Token.TokenType.DIV) ||
                peek(0).getTokenType().equals(Token.TokenType.MOD)) {
            mulExpNode.addNode(parseToken());
            mulExpNode.addNode(parseUnaryExp());
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
            if (!peek(0).getTokenType().equals(Token.TokenType.RPARENT)) {
                unaryExpNode.addNode(parseFuncRParams());
            }
            unaryExpNode.addNode(parseToken()); // ')'
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

            primaryExpNode.addNode(parseToken()); // ')'
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
        while(peek(0).getTokenType().equals(Token.TokenType.COMMA)) {
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
