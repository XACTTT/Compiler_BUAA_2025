package frontend.ast;

import java.util.ArrayList;

/*
变量声明。
VarDecl -> [ 'static' ] BType VarDef { ',' VarDef } ';'
*/
public class VarDeclNode extends DECLnode{
    public final boolean isStatic;
    public final BTypeNode bType;
    public final ArrayList<VarDefNode> varDefs;
    public VarDeclNode(boolean isStatic, BTypeNode bType, ArrayList<VarDefNode> varDefs) {
        this.isStatic = isStatic;
        this.bType = bType;
        this.varDefs = varDefs;
    }
}
