package backend.llvm;

public abstract class IRInstruction {
    protected IRValue result;
    
    public IRInstruction() {
    }
    
    public IRInstruction(IRValue result) {
        this.result = result;
    }
    
    public IRValue getResult() {
        return result;
    }
    
    public abstract String toString();
}
