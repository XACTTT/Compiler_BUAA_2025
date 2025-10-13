package frontend.ast;
/*
主函数定义。
MainFuncDef -> 'int' 'main' '(' ')' Block
*/
public class MainFuncDefNode extends ASTnode {
    public final BlockNode body;

    public MainFuncDefNode() {
        this.body = new BlockNode();
    }
}
