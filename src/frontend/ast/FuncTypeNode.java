package frontend.ast;

import frontend.Token;

/*
函数返回类型
FuncType -> 'void' | 'int'
*/
public class FuncTypeNode extends TerminalNode {
    public final Token type;

    public FuncTypeNode(Token type) {
        this.type = type;
    }
}
