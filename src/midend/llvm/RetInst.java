package midend.llvm;

public class RetInst extends IRInstruction {
    private IRValue returnValue;
    private IRType returnType;
    
    public RetInst(IRType returnType, IRValue returnValue) {
        this.returnType = returnType;
        this.returnValue = returnValue;
    }
    
    public RetInst() {
        this.returnType = IRType.VOID;
    }
    
    @Override
    public String toString() {
        if (returnType.isVoid()) {
            return "ret void";
        } else {
            return "ret " + returnValue.toStringWithType();
        }
    }
}
