package frontend.ast;

import java.util.ArrayList;

public abstract class ASTnode {
    public int lineNumber; // 可以考虑添加行号
    public SyntaxType syntaxType;
    public ArrayList<ASTnode> children = new ArrayList<>();
    public boolean printSign;
    public ASTnode( SyntaxType syntaxType) {
        this.syntaxType = syntaxType;
        this.printSign = true;
    }

    public ASTnode() {

    }

    public void addNode(ASTnode node) {
        children.add(node);
    }

    @Override
    public String toString() {
        ArrayList<String> childStrings = new ArrayList<>();
        for (ASTnode child : children) {
            if (child != null) {
                String childString = child.toString();
                if (childString != null && !childString.isEmpty()) {
                    childStrings.add(childString);
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < childStrings.size(); i++) {
            sb.append(childStrings.get(i));
            if (i < childStrings.size() - 1) {
                sb.append("\n");
            }
        }
        if (printSign) {
            if (!childStrings.isEmpty()) {
                sb.append("\n");
            }
            sb.append("<").append(this.syntaxType.getTypeName()).append(">");
        }

        return sb.toString();
    }
}
