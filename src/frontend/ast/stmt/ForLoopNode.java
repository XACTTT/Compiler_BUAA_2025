package frontend.ast.stmt;

import frontend.ast.ASTnode;
import frontend.ast.SyntaxType;

public class ForLoopNode extends ASTnode {
    public ForLoopNode() {
        super(SyntaxType.STMT);
        this.printSign=false;
    }
}
