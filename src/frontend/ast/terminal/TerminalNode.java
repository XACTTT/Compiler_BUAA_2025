package frontend.ast.terminal;

import frontend.Token;
import frontend.ast.ASTnode;
import frontend.ast.SyntaxType;

public class TerminalNode extends ASTnode {
    public Token token;
    public TerminalNode(SyntaxType type) {
        super(type);
    }



    public int getLine() {
        return token.lineNumber;
    }

    @Override
    public String toString() {
        return token.toString();
    }
}
