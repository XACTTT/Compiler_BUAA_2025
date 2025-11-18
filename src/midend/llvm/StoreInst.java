package midend.llvm;

public class StoreInst extends IRInstruction {
    private IRValue value;
    private IRValue pointer;
    
    public StoreInst(IRValue value, IRValue pointer) {
        this.value = value;
        this.pointer = pointer;
    }
    
    @Override
    public String toString() {
        return "store " + value.toStringWithType() + ", " + pointer.toStringWithType();
    }
}
