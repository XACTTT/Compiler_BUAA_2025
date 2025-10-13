package frontend.ast;

import frontend.Token;

/*
基本类型
BType -> 'int'
*/
public class BTypeNode extends ASTnode{
    public final Token type;

    public BTypeNode(Token type) {
        this.type = type;
    }
}
