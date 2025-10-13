package frontend.ast;

import frontend.Token;
import java.util.Optional;

/*
左值表达式 Ident ['[' Exp ']']
*/
public class LValNode extends EXPnode {
    public final Token identifier;
    public final Optional<EXPnode> arrayIndex;

    public LValNode(Token identifier, Optional<EXPnode> arrayIndex) {
        this.identifier = identifier;
        this.arrayIndex = arrayIndex;
    }
}