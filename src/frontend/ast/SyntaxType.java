package frontend.ast;

public enum SyntaxType {
    COMP_UNIT("CompUnit");
    private final String typeName;

    SyntaxType(String typeName) {
        this.typeName = typeName;
    }
}
