package frontend.ast;

import frontend.ast.stmt.STMTnode;

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


}
