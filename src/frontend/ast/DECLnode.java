package frontend.ast;

public  class DECLnode extends ASTnode{
    public DECLnode(SyntaxType type) {
        super(type);
        this.printSign = false;
    }
}
