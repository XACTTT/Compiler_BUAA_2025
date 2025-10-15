package frontend.ast.stmt;

import frontend.Token;
import frontend.ast.SyntaxType;

/*
赋值语句 LVal '=' Exp ';'
*/
public class AssignStmtNode extends STMTnode {
public AssignStmtNode() {
    super(SyntaxType.STMT);
}
}