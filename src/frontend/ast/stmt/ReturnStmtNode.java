package frontend.ast.stmt;

import frontend.ast.EXPnode;

import java.util.Optional;

/*
Return 语句 'return' [Exp] ';'
*/
public class ReturnStmtNode extends STMTnode {
    public final Optional<EXPnode> returnValue;

    public ReturnStmtNode(Optional<EXPnode> returnValue) {
        this.returnValue = returnValue;
    }
}
