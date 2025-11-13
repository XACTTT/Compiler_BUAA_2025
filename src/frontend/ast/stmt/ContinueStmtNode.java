package frontend.ast.stmt;

import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class ContinueStmtNode extends STMTnode {
    public ContinueStmtNode(){
        super(SyntaxType.STMT);
        this.printSign=false;
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
