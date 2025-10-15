package frontend.ast;

import frontend.ast.terminal.BTypeNode;

import java.util.ArrayList;

/*
常量声明。
ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
*/
public class ConstDeclNode extends DECLnode{
    public ConstDeclNode() {
        super(SyntaxType.CONST_DECL);
    }
}
