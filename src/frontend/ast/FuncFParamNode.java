package frontend.ast;

import frontend.Token;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class FuncFParamNode extends ASTnode{
    public FuncFParamNode() {
        super(SyntaxType.FUNC_FORMAL_PARAM);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
