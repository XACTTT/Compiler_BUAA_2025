package frontend.ast;

import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class FuncRParamsNode extends ASTnode{
    public FuncRParamsNode(){
        super(SyntaxType.FUNC_REAL_PARAM_S);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
