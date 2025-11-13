package frontend.ast;

import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class FuncFParamsNode extends ASTnode{
    public FuncFParamsNode(){
        super(SyntaxType.FUNC_FORMAL_PARAM_S);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
