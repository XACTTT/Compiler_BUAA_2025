package frontend.ast;
import frontend.Token;
import java.util.Optional;
import java.util.ArrayList;
/*
常量定义。
ConstDef -> Ident [ '[' ConstExp ']' ] '=' ConstInitVal
*/
public class ConstDefNode extends ASTnode{
    public final Token identifier;
    public final Optional<ConstExpNode> arraySize;
    public final InitValNode constInitVal;

    public ConstDefNode(Token identifier, Optional<ConstExpNode> arraySize, InitValNode constInitVal) {
        this.identifier = identifier;
        this.arraySize = arraySize;
        this.constInitVal = constInitVal;
    }
}
