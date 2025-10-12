package frontend.ast;

import frontend.Token;

/*
一元运算 UnaryOp UnaryExp
*/
public class UnaryOpNode extends EXPnode {
    public final Token operator;
    public final EXPnode operand;

    public UnaryOpNode(Token operator, EXPnode operand) {
        this.operator = operator;
        this.operand = operand;
    }
}
