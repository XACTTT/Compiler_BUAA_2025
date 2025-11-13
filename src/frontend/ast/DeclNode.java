package frontend.ast;

import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public  class DeclNode extends ASTnode{
    public DeclNode(SyntaxType type) {
        super(type);
        this.printSign = false;
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
