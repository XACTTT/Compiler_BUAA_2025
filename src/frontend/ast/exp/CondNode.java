package frontend.ast.exp;

import frontend.ast.ASTnode;
import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class CondNode extends ASTnode {
    public CondNode() {
        super(SyntaxType.COND_EXP);
    }

    public SymbolType accept(SemanticAnalyzer visitor) {
        return visitor.visit(this);
    }
}
