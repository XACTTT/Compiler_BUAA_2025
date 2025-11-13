package frontend.ast;

import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class ConstInitValNode extends ASTnode {
        public ConstInitValNode() {
        super(SyntaxType.CONST_INIT_VAL);
        }
        public SymbolType accept(SemanticAnalyzer visitor) {
                visitor.visit(this);
                return null;
        }
}
