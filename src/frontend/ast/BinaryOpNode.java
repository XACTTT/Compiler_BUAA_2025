package frontend.ast;

import frontend.Token;

/*
所有二元运算的通用节点
*/
public class BinaryOpNode extends EXPnode {
    public final EXPnode left;
    public final Token operator;
    public final EXPnode right;

    public BinaryOpNode(EXPnode left, Token operator, EXPnode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
}
