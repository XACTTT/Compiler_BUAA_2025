package frontend.ast.exp;

import frontend.Token;
import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class UnaryExpNode extends EXPnode{
    public UnaryExpNode() {
        super(SyntaxType.UNARY_EXP);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        return visitor.visit(this);
    }
}
