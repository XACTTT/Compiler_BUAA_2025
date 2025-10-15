package frontend.ast.stmt;


import frontend.ast.SyntaxType;

/**
 * For 语句 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
 */
public class ForStmtNode extends STMTnode {
public ForStmtNode() {
    super(SyntaxType.FOR_STMT);
}


}
