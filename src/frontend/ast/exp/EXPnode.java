package frontend.ast.exp;

import frontend.ast.ASTnode;
import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

import javax.swing.text.Position;

public class EXPnode extends ASTnode {
    public EXPnode(SyntaxType type) {
        super(type);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
      return   visitor.visit(this);
    }
}
