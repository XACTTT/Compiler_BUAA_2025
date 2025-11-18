package midend.llvm;

public class BranchInst extends IRInstruction {
    private IRValue condition;
    private String trueLabel;
    private String falseLabel;
    private String unconditionalLabel;
    
    // Conditional branch
    public BranchInst(IRValue condition, String trueLabel, String falseLabel) {
        this.condition = condition;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }
    
    // Unconditional branch
    public BranchInst(String label) {
        this.unconditionalLabel = label;
    }
    
    @Override
    public String toString() {
        if (unconditionalLabel != null) {
            return "br label %" + unconditionalLabel;
        } else {
            return "br " + condition.toStringWithType() + ", label %" + trueLabel + 
                   ", label %" + falseLabel;
        }
    }
}
