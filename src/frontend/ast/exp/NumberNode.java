package frontend.ast.exp;

import frontend.ast.SyntaxType;

/*
数值 Number -> IntConst
*/
public class NumberNode extends EXPnode {
    public NumberNode() {
        super(SyntaxType.INT_CONST);
    }
}
