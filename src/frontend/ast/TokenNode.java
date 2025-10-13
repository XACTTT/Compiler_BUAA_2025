package frontend.ast;

import frontend.Token;

public class TokenNode extends TerminalNode{
    private final Token token;
    public TokenNode(Token token) {
        this.token = token;
    }
}
