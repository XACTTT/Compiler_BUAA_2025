package frontend.ast.exp;

import frontend.Token;
import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

/*
一元运算 UnaryOp UnaryExp
*/
public class UnaryOpNode extends EXPnode {
public UnaryOpNode() {
    super(SyntaxType.UNARY_OP);
}
    public SymbolType accept(SemanticAnalyzer visitor) {
        return visitor.visit(this);
    }
}
