package frontend.ast.stmt;

import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

/*
表达式语句 [Exp] ';'
*/
public class ExpStmtNode extends STMTnode {
public ExpStmtNode() {
    super(SyntaxType.STMT);
    this.printSign=false;
}
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
