package frontend.ast;

import frontend.Token;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

import java.util.Optional;
/*
变量定义。
VarDef -> Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
*/
public class VarDefNode extends ASTnode{
    public VarDefNode(){
        super(SyntaxType.VAR_DEF);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
