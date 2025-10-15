package frontend.ast;
/*
主函数定义。
MainFuncDef -> 'int' 'main' '(' ')' Block
*/
public class MainFuncDefNode extends ASTnode {


    public MainFuncDefNode() {
    super(SyntaxType.MAIN_FUNC_DEF);
    }
}
