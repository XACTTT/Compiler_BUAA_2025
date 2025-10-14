package frontend.ast.terminal;

import frontend.Token;
import frontend.ast.ASTnode;

/*
基本类型
BType -> 'int'
*/
public class BTypeNode extends TerminalNode {
    public final Token type;

    public BTypeNode(Token type) {
        this.type = type;
    }
}
