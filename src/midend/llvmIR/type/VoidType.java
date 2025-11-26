package midend.llvmIR.type;

public class VoidType extends Type {
    @Override
    public String toString() {
        return "void";
    }

    @Override
    public boolean isVoid() {
        return true;
    }
}
