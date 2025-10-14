package frontend;

import frontend.ast.*;
import frontend.ast.stmt.STMTnode;
import frontend.ast.terminal.BTypeNode;
import frontend.ast.terminal.TokenNode;

import java.util.ArrayList;

public class Parser {
    private ArrayList<Token> tokens;
    private int currentIndex;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        currentIndex = 0;
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
        funcDefNode.addNode(parseFuncFParam());
        while (!getCurrentToken().getTokenType().equals(Token.TokenType.RPARENT)){
            funcDefNode.addNode(parseToken());
            funcDefNode.addNode(parseFuncFParam());
        }
        funcDefNode.addNode(parseToken());
        funcDefNode.addNode(parseBlock());
    }
    return funcDefNode;
    }
    public DECLnode parseDecl() {
    DECLnode declNode = new DECLnode();
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

    }
}
