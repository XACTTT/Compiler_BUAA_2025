package midend;

import frontend.ast.CompUnitNode;
import midend.symbol.SymbolTable;

public class MidEnd {
    private SemanticAnalyzer analyzer=new SemanticAnalyzer();
    private final CompUnitNode root;
    public MidEnd(CompUnitNode root) {
        this.root = root;
    }
    public void GenerateSymbolTable(){
            root.accept(analyzer);
    }
    public SymbolTable GetSymbolTable(){
        return analyzer.getSymbolTable();
    }
}
