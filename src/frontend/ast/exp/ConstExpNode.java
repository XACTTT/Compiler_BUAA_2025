package frontend.ast.exp;

import frontend.ast.ASTnode;
import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class ConstExpNode extends EXPnode {
public ConstExpNode(){
    super(SyntaxType.CONST_EXP);
}
    public SymbolType accept(SemanticAnalyzer visitor) {
        return visitor.visit(this);
    }
}
