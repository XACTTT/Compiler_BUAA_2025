package frontend.ast;
import frontend.Token;
import java.util.Optional;
import java.util.ArrayList;
/*
常量定义。
ConstDef -> Ident [ '[' ConstExp ']' ] '=' ConstInitVal
*/
public class ConstDefNode extends ASTnode{
    public ConstDefNode() {
        super(SyntaxType.CONST_DEF);
    }


}
