package frontend.ast;

/*
常量声明。
ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
*/
public class ConstDeclNode extends ASTnode {
    public ConstDeclNode() {
        super(SyntaxType.CONST_DECL);
    }
}
