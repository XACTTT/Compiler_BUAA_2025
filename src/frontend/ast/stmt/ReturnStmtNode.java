package frontend.ast.stmt;

import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;
import org.w3c.dom.Node;

/*
Return 语句 'return' [Exp] ';'
*/
public class ReturnStmtNode extends STMTnode {
    public ReturnStmtNode() {
        super(SyntaxType.STMT);
        this.printSign=false;
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
