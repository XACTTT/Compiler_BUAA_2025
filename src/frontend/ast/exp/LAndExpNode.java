package frontend.ast.exp;

import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class LAndExpNode extends EXPnode{
    public LAndExpNode(){
        super(SyntaxType.LAND_EXP);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        return visitor.visit(this);
    }
}
