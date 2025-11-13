package frontend.ast.stmt;


import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

/**
 * For 语句 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
 */
public class ForStmtNode extends STMTnode {
public ForStmtNode() {
    super(SyntaxType.FOR_STMT);
}

    public SymbolType accept(SemanticAnalyzer visitor) {
        visitor.visit(this);
        return null;
    }
}
