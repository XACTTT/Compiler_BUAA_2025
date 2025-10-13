package frontend.ast;

import java.util.ArrayList;

public abstract class ASTnode {
    public int lineNumber; // 可以考虑添加行号
    public ArrayList<ASTnode> children = new ArrayList<>();
    public void addNode(ASTnode node) {
        children.add(node);
    }
}
