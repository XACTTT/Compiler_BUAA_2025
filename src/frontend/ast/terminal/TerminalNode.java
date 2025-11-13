package frontend.ast.terminal;

import frontend.Token;
import frontend.ast.ASTnode;
import frontend.ast.SyntaxType;
import midend.SemanticAnalyzer;
import midend.symbol.SymbolType;

public class TerminalNode extends ASTnode {
    public Token token;
    public TerminalNode(SyntaxType type) {
        super(type);
    }

    public SymbolType accept(SemanticAnalyzer visitor) {
        return visitor.visit(this);
    }

    public int getLine() {
        return token.lineNumber;
    }

    @Override
    public String toString() {
        // 直接返回Token的字符串表示，不附加任何换行符
        if (token != null) {
            return token.toString();
        }
        return "";
    }
}
