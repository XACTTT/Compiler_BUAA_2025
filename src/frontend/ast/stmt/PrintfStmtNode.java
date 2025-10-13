package frontend.ast.stmt;

import frontend.Token;
import frontend.ast.EXPnode;

import java.util.ArrayList;

/*
Printf 语句 'printf''('StringConst {','Exp}')'';'
*/
public class PrintfStmtNode extends STMTnode {
    public final Token formatString;
    public final ArrayList<EXPnode> params;

    public PrintfStmtNode(Token formatString, ArrayList<EXPnode> params) {
        this.formatString = formatString;
        this.params = params;
    }
}
