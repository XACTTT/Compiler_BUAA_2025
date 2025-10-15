package frontend.ast;

import frontend.ast.exp.EXPnode;

/*
左值表达式 Ident ['[' Exp ']']
*/
public class LValNode extends EXPnode {
    public LValNode() {
        super(SyntaxType.LVAL_EXP);
    }
}