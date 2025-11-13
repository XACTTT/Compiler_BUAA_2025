package frontend.ast.stmt;

import frontend.ast.ASTnode;
import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class ForLoopNode extends ASTnode {
    public ForLoopNode() {
        super(SyntaxType.STMT);
        this.printSign=false;
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
