package frontend.ast.exp;

import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class LOrExpNode extends EXPnode {
    public LOrExpNode() {
        super(SyntaxType.LOR_EXP);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        return visitor.visit(this);
    }
}
