package frontend.ast;

import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

/*
主函数定义。
MainFuncDef -> 'int' 'main' '(' ')' Block
*/
public class MainFuncDefNode extends ASTnode {

    public MainFuncDefNode() {
    super(SyntaxType.MAIN_FUNC_DEF);
    }

    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
