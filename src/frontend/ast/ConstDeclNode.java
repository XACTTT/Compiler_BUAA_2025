package frontend.ast;

import java.util.ArrayList;

/*
常量声明。
ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
*/
public class ConstDeclNode extends DECLnode{
    public final BTypeNode bType;
    public final ArrayList<ConstDefNode> constDefs;

    public ConstDeclNode(BTypeNode bType, ArrayList<ConstDefNode> constDefs) {
        this.bType = bType;
        this.constDefs = constDefs;
    }
}
