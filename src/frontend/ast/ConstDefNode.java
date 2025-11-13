package frontend.ast;
import frontend.Token;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

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
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }

}
