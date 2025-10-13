package frontend.ast.stmt;

import frontend.ast.EXPnode;

import java.util.Optional;

/*
表达式语句 [Exp] ';'
*/
public class ExpStmtNode extends STMTnode {
    public final Optional<EXPnode> expression;

    public ExpStmtNode(Optional<EXPnode> expression) {
        this.expression = expression;
    }
}
