package frontend.ast.stmt;

import frontend.ast.SyntaxType;

/*
表达式语句 [Exp] ';'
*/
public class ExpStmtNode extends STMTnode {
public ExpStmtNode() {
    super(SyntaxType.STMT);
}
}
