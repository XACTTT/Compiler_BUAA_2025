package frontend.ast.terminal;

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

    public String getTokenValue() {
        return ((TokenNode)children.get(0)).getToken().getValue();
    }
}
