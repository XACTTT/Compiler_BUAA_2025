package backend.llvm;

public class AllocaInst extends IRInstruction {
    private IRType allocatedType;
    
    public AllocaInst(IRValue result, IRType allocatedType) {
        super(result);
        this.allocatedType = allocatedType;
    }
    
    @Override
    public String toString() {
        return result.getName() + " = alloca " + allocatedType;
    }
}
