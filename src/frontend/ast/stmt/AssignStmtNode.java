package frontend.ast.stmt;

import frontend.ast.EXPnode;
import frontend.ast.LValNode;

/*
赋值语句 LVal '=' Exp ';'
*/
public class AssignStmtNode extends STMTnode {
    public final LValNode leftValue;
    public final EXPnode expression;

    public AssignStmtNode(LValNode leftValue, EXPnode expression) {
        this.leftValue = leftValue;
        this.expression = expression;
    }
}