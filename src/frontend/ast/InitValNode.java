package frontend.ast;
import java.util.List;


public abstract class InitValNode extends ASTnode {}

class ExpInitValNode extends InitValNode {
    public final EXPnode expression;

    public ExpInitValNode(EXPnode expression) {
        this.expression = expression;
    }
}

class ArrayInitValNode extends InitValNode {
    public final List<InitValNode> values;

    public ArrayInitValNode(List<InitValNode> values) {
        this.values = values;
    }
}