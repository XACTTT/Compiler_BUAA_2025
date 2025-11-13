package frontend.ast.exp;
import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;


public class AddExpNode extends EXPnode {
    public AddExpNode() {
        super(SyntaxType.ADD_EXP);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        return visitor.visit(this);
    }
}
