package frontend.ast.exp;

import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class RelExpNode extends EXPnode{
    public RelExpNode() {
        super(SyntaxType.REL_EXP);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        return visitor.visit(this);
    }
}
