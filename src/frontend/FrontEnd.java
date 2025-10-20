package frontend;

import error.MyErrorHandler;
import frontend.ast.CompUnitNode;

import java.util.ArrayList;

public class FrontEnd {
    public  Lexer lexer;
    public  Parser parser;
    private  String source;
    private MyErrorHandler errorHandler;
    private CompUnitNode root;

    public FrontEnd(String source, MyErrorHandler errorHandler) {
        this.source = source;
        this.errorHandler = errorHandler;
    }

    public void GenerateTokenList(){
        lexer = new Lexer(source);
        lexer.generateTokens();
    }

    public void GenerateAstTree(){
        parser = new Parser(lexer.getTokens());
        root = parser.parseCompUnit();
    }

    public CompUnitNode getAstTree(){
        return root;
    }

    public ArrayList<Token> getTokenList(){
        return lexer.getTokens();
    }
}
