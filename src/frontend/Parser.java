package frontend;

import frontend.ast.*;

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
    }
    public DECLnode parseDecl() {

    }
    public FuncTypeNode parseFuncType() {
        return new FuncTypeNode(readToken());

    }
    public IdentNode parseIdent() {
        return new IdentNode(readToken());
    }

    public TokenNode parseToken() {
        return new TokenNode(readToken());
    }

    public BlockNode parseBlock() {}

    public FuncFParamNode parseFuncFParam() {}
}
