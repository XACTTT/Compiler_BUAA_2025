package frontend.ast;

import frontend.Token;

/*
数值 Number -> IntConst
*/
public class NumberNode extends EXPnode {
    public final Token intConst;

    public NumberNode(Token intConst) {
        this.intConst = intConst;
    }
}
