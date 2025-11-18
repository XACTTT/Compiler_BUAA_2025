package midend.llvm;

public class LoadInst extends IRInstruction {
    private IRType loadType;
    private IRValue pointer;
    
    public LoadInst(IRValue result, IRType loadType, IRValue pointer) {
        super(result);
        this.loadType = loadType;
        this.pointer = pointer;
    }
    
    @Override
    public String toString() {
        return result.getName() + " = load " + loadType + ", " + pointer.toStringWithType();
    }
}
