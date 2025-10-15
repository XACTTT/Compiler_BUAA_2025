package frontend.ast.exp;

import frontend.Token;
import frontend.ast.SyntaxType;

public class UnaryExpNode extends EXPnode{
    public UnaryExpNode() {
        super(SyntaxType.UNARY_EXP);
    }
}
