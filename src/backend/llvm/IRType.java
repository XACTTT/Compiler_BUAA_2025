package backend.llvm;

public class IRType {
    private String type;
    
    // Common types
    public static final IRType I32 = new IRType("i32");
    public static final IRType VOID = new IRType("void");
    public static final IRType I8 = new IRType("i8");
    
    private IRType(String type) {
        this.type = type;
    }
    
    public static IRType getPointerType(IRType baseType) {
        return new IRType(baseType.toString() + "*");
    }
    
    public static IRType getArrayType(IRType elemType, int size) {
        return new IRType("[" + size + " x " + elemType.toString() + "]");
    }
    
    public boolean isVoid() {
        return type.equals("void");
    }
    
    public boolean isPointer() {
        return type.endsWith("*");
    }
    
    @Override
    public String toString() {
        return type;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IRType) {
            return this.type.equals(((IRType) obj).type);
        }
        return false;
    }
}
