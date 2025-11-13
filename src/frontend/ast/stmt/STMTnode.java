package frontend.ast.stmt;

import frontend.ast.ASTnode;
import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public  class STMTnode extends ASTnode {
    public STMTnode(SyntaxType type) {
        super(type);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
         visitor.visit(this);
         return null;
    }
}
