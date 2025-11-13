package frontend.ast;

import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

import java.util.ArrayList;

public class CompUnitNode extends ASTnode{

    public CompUnitNode() {
        super(SyntaxType.COMP_UNIT);
    }

    public ArrayList<DeclNode> getDecls() {
        ArrayList<DeclNode> decls = new ArrayList<>();
        for (ASTnode child : children) {
            if (child instanceof DeclNode) {
                decls.add((DeclNode) child);
            }
        }
        return decls;
    }

    public ArrayList<FuncDefNode> getFuncDefs() {
        ArrayList<FuncDefNode> funcDefs = new ArrayList<>();
        for (ASTnode child : children) {
            if (child instanceof FuncDefNode) {
                funcDefs.add((FuncDefNode) child);
            }
        }
        return funcDefs;
    }

    public MainFuncDefNode getMainFuncDef() {
    for (ASTnode child : children) {
        if (child instanceof MainFuncDefNode) {
            return (MainFuncDefNode) child;
        }
    }
    return null;
    }

    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
