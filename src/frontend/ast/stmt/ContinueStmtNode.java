package frontend.ast.stmt;

import frontend.ast.SyntaxType;

public class ContinueStmtNode extends STMTnode {
    public ContinueStmtNode(){
        super(SyntaxType.STMT);
        this.printSign=false;
    }
}
