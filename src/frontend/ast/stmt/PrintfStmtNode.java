package frontend.ast.stmt;

import frontend.ast.SyntaxType;

/*
Printf 语句 'printf''('StringConst {','Exp}')'';'
*/
public class PrintfStmtNode extends STMTnode {
public PrintfStmtNode(){
    super(SyntaxType.STMT);
}
}
