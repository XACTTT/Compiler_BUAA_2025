package frontend.ast.terminal;

import frontend.Token;
import frontend.ast.ASTnode;
import frontend.ast.SyntaxType;

/*
函数返回类型
FuncType -> 'void' | 'int'
*/
public class FuncTypeNode extends ASTnode {

    public FuncTypeNode() {
        super(SyntaxType.FUNC_TYPE);
    }
}
