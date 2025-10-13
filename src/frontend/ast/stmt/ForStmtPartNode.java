package frontend.ast.stmt;

import frontend.ast.ASTnode;

import java.util.List;

/*
For 语句中的 LVal '=' Exp 部分
*/
public class ForStmtPartNode extends ASTnode {
    public final List<AssignStmtNode> assignments;

    public ForStmtPartNode(List<AssignStmtNode> assignments) {
        this.assignments = assignments;
    }
}