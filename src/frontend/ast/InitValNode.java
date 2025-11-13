package frontend.ast;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

import java.util.List;


public  class InitValNode extends ASTnode {
    public InitValNode() {
        super(SyntaxType.INIT_VAL);

    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}


