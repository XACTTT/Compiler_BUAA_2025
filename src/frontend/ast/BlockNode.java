package frontend.ast;

import frontend.ast.stmt.STMTnode;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

import java.util.ArrayList;

/*
语句块。
Block -> '{' { BlockItem } '}'
BlockItem -> Decl | Stmt
*/
public class BlockNode extends ASTnode {
public BlockNode() {
    super(SyntaxType.BLOCK);
}
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }

}
