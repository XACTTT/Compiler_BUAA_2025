package frontend.ast;
import frontend.Token;

import java.util.ArrayList;
import java.util.Optional;

/*
函数定义.
FuncDef -> FuncType Ident '(' [FuncFParams] ')' Block
*/
public class FuncDefNode extends ASTnode {

    public FuncDefNode() {
        super(SyntaxType.FUNC_DEF);
    }

}
