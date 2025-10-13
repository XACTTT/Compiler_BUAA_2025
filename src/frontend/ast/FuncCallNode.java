package frontend.ast;

import frontend.Token;

import java.util.ArrayList;
import java.util.Optional;

/*
函数调用 Ident '(' [FuncRParams] ')' 从一元表达式里抽离出来
*/
public class FuncCallNode extends EXPnode {
    public final Token identifier;
    public final Optional<ArrayList<EXPnode>> params;

    public FuncCallNode(Token identifier, Optional<ArrayList<EXPnode>> params) {
        this.identifier = identifier;
        this.params = params;
    }
}