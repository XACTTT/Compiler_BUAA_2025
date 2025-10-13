package frontend.ast;

import frontend.Token;

public class FuncFParamNode extends ASTnode{
    public final BTypeNode bType;
    public final Token identifier;
    public final boolean isArray;

    public FuncFParamNode(BTypeNode bType, Token identifier, boolean isArray) {
        this.bType = bType;
        this.identifier = identifier;
        this.isArray = isArray;
    }
}
