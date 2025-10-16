package frontend.ast;

public  class DeclNode extends ASTnode{
    public DeclNode(SyntaxType type) {
        super(type);
        this.printSign = false;
    }
}
