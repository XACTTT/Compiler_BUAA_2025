package midend.llvm;

public class IRValue {
    private IRType type;
    private String name;
    private boolean isConstant;
    private Integer constantValue;
    
    public IRValue(IRType type, String name) {
        this.type = type;
        this.name = name;
        this.isConstant = false;
    }
    
    public IRValue(int value) {
        this.type = IRType.I32;
        this.constantValue = value;
        this.isConstant = true;
        this.name = String.valueOf(value);
    }
    
    public IRType getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isConstant() {
        return isConstant;
    }
    
    public Integer getConstantValue() {
        return constantValue;
    }
    
    @Override
    public String toString() {
        if (isConstant) {
            return String.valueOf(constantValue);
        }
        return name;
    }
    
    public String toStringWithType() {
        return type + " " + toString();
    }
}
