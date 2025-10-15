package frontend.ast.stmt;

import frontend.ast.SyntaxType;

/**
 * If 语句 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
 */
public class IfStmtNode extends STMTnode {
public IfStmtNode(){
    super(SyntaxType.STMT);
}
}
