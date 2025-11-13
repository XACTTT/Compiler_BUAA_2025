package frontend.ast.stmt;

import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class BreakStmtNode extends STMTnode {
    public BreakStmtNode() {
        super(SyntaxType.STMT);
        this.printSign=false;
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
