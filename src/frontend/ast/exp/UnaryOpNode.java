package frontend.ast.exp;

import frontend.Token;
import frontend.ast.SyntaxType;

/*
一元运算 UnaryOp UnaryExp
*/
public class UnaryOpNode extends EXPnode {
public UnaryOpNode() {
    super(SyntaxType.UNARY_OP);
}
}
