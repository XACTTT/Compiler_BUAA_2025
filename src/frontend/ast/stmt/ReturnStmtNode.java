package frontend.ast.stmt;

import frontend.ast.SyntaxType;
import org.w3c.dom.Node;

/*
Return 语句 'return' [Exp] ';'
*/
public class ReturnStmtNode extends STMTnode {
    public ReturnStmtNode() {
        super(SyntaxType.STMT);
    }

}
