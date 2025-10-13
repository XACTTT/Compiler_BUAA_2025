package frontend.ast.stmt;

import frontend.ast.EXPnode;

import java.util.Optional;

/**
 * If 语句 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
 */
public class IfStmtNode extends STMTnode {
    public final EXPnode condition;
    public final STMTnode thenStmt;
    public final Optional<STMTnode> elseStmt;

    public IfStmtNode(EXPnode condition, STMTnode thenStmt, Optional<STMTnode> elseStmt) {
        this.condition = condition;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
    }
}
