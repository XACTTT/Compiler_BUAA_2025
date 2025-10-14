package frontend.ast;

import frontend.Token;
import frontend.ast.terminal.TerminalNode;

public class IdentNode extends TerminalNode {
    private final Token token;
    public IdentNode(Token token) {
        this.token = token;
    }
}
