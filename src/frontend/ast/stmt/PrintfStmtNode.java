package frontend.ast.stmt;

import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

/*
Printf 语句 'printf''('StringConst {','Exp}')'';'
*/
public class PrintfStmtNode extends STMTnode {
public PrintfStmtNode(){
    super(SyntaxType.STMT);
    this.printSign=false;
}
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
