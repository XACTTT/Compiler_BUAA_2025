package frontend.ast;

import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

/*
变量声明。
VarDecl -> [ 'static' ] BType VarDef { ',' VarDef } ';'
*/
public class VarDeclNode extends ASTnode {
public VarDeclNode(){
    super(SyntaxType.VAR_DECL);
}

    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
