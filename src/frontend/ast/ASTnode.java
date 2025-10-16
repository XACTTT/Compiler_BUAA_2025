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

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (ASTnode astNode : this.children) {
            stringBuilder.append(astNode);
            stringBuilder.append("\n");
        }

        if (printSign) {
            stringBuilder.append("<" + this.syntaxType.getTypeName() + ">");
        } else {
            if (!stringBuilder.isEmpty() &&
                    stringBuilder.charAt(stringBuilder.length() - 1) == '\n') {
                stringBuilder.setLength(stringBuilder.length() - 1);
            }
        }

        return stringBuilder.toString();
    }
}
