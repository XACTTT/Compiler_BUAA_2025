package frontend.ast.exp;

import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class PrimaryExpNode extends EXPnode{
    public PrimaryExpNode() {
        super(SyntaxType.PRIMARY_EXP);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        return visitor.visit(this);
    }
}
