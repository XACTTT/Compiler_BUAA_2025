package frontend.ast.stmt;


import frontend.ast.EXPnode;

import java.util.Optional;

/**
 * For 语句 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
 */
public class ForStmtNode extends STMTnode {
    public final Optional<ForStmtPartNode> init;
    public final Optional<EXPnode> condition;
    public final Optional<ForStmtPartNode> post;
    public final STMTnode body;

    public ForStmtNode(Optional<ForStmtPartNode> init, Optional<EXPnode> condition, Optional<ForStmtPartNode> post, STMTnode body) {
        this.init = init;
        this.condition = condition;
        this.post = post;
        this.body = body;
    }
}
