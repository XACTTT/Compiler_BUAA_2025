package frontend.ast.terminal;

import frontend.Token;
import frontend.ast.SyntaxType;

import javax.swing.text.Position;

public class IdentNode extends TerminalNode {

    public IdentNode(Token token) {
        super(SyntaxType.IDENT);
        this.token = token;
    }
}
