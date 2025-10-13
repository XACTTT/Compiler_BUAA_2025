package frontend.ast;

import frontend.Token;
import java.util.Optional;
/*
变量定义。
VarDef -> Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
*/
public class VarDefNode extends ASTnode{
    public final Token identifier;
    public final Optional<EXPnode> arraySize;
    public final Optional<InitValNode> initVal;

    public VarDefNode(Token identifier, Optional<EXPnode> arraySize, Optional<InitValNode> initVal) {
        this.identifier = identifier;
        this.arraySize = arraySize;
        this.initVal = initVal;
    }
}
