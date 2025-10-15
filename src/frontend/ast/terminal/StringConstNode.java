package frontend.ast.terminal;

import frontend.Token;
import frontend.ast.SyntaxType;

public class StringConstNode extends TerminalNode {


    public StringConstNode(Token token) {
        super(SyntaxType.STRING_CONST);
        this.token = token;
    }
}
