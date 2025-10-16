package frontend.ast;

/*
变量声明。
VarDecl -> [ 'static' ] BType VarDef { ',' VarDef } ';'
*/
public class VarDeclNode extends ASTnode {
public VarDeclNode(){
    super(SyntaxType.VAR_DECL);
}
}
