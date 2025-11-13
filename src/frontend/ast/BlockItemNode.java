package frontend.ast;

import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class BlockItemNode extends ASTnode{

public BlockItemNode(){
    super(SyntaxType.BLOCK_ITEM);
    this.printSign = false;
}

    @Override
    public SymbolType accept(SemanticAnalyzer visitor) {
        return super.accept(visitor);
    }
}
