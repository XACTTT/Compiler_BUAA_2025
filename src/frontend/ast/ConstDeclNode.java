package frontend.ast;

import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

/*
常量声明。
ConstDecl -> 'const' BType ConstDef { ',' ConstDef } ';'
*/
public class ConstDeclNode extends ASTnode {
    public ConstDeclNode() {
        super(SyntaxType.CONST_DECL);
    }
    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
