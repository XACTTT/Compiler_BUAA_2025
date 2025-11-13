package frontend.ast.stmt;

import frontend.Token;
import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

/*
赋值语句 LVal '=' Exp ';'
*/
public class AssignStmtNode extends STMTnode {
public AssignStmtNode() {
    super(SyntaxType.STMT);
    this.printSign=false;
}
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}