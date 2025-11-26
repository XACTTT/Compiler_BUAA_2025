package midend.llvmIR.type;

/**
 * PointerType - 指针类型
 * pointedType: 指向的类型（如i32* 中的i32）
 */
public class PointerType extends Type {
    private final Type pointedType;

    public PointerType(Type pointedType) {
        this.pointedType = pointedType;
    }

    public Type getPointedType() {
        return pointedType;
    }

    @Override
    public String toString() {
        return pointedType.toString() + "*";
    }

    @Override
    public boolean isPointer() {
        return true;
    }
}
