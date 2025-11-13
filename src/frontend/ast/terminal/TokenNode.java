package frontend.ast.terminal;

import frontend.Token;
import frontend.ast.SyntaxType;

public class TokenNode extends TerminalNode {



    public TokenNode(Token token) {
        super(SyntaxType.TOKEN);
        this.token = token;
    }

    public Token getToken(){
        return token;
    }
}
