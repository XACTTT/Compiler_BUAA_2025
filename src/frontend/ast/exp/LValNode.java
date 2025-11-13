package frontend.ast.exp;

import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

/*
左值表达式 Ident ['[' Exp ']']
*/
public class LValNode extends EXPnode {
    public LValNode() {
        super(SyntaxType.LVAL_EXP);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
     return    visitor.visit(this);
    }
}