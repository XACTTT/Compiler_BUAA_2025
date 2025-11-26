package midend.llvmIR.type;

public class LabelType extends Type {
    @Override
    public String toString() {
        return "label";
    }

    @Override
    public boolean isLabel() {
        return true;
    }
}