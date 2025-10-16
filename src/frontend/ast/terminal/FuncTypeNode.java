package frontend.ast.terminal;

import frontend.Token;
import frontend.ast.SyntaxType;

/*
函数返回类型
FuncType -> 'void' | 'int'
*/
public class FuncTypeNode extends TerminalNode {




    public FuncTypeNode(Token token) {
        super(SyntaxType.FUNC_TYPE);
        this.token = token;
        this.printSign = true;
    }
}
