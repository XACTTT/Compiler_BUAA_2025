package frontend.ast.terminal;

import frontend.Token;
import frontend.ast.ASTnode;
import frontend.ast.SyntaxType;

/*
基本类型
BType -> 'int'
*/
public class BTypeNode extends TerminalNode {


    public BTypeNode(Token token) {
        super(SyntaxType.BTYPE);
        this.printSign = false;
    }
}
