package backend.llvm;

public class IcmpInst extends IRInstruction {
    public enum Condition {
        EQ("eq"),
        NE("ne"),
        SGT("sgt"),
        SGE("sge"),
        SLT("slt"),
        SLE("sle");
        
        private String condStr;
        
        Condition(String condStr) {
            this.condStr = condStr;
        }
        
        public String getCondStr() {
            return condStr;
        }
    }
    
    private Condition condition;
    private IRValue operand1;
    private IRValue operand2;
    
    public IcmpInst(IRValue result, Condition condition, IRValue operand1, IRValue operand2) {
        super(result);
        this.condition = condition;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }
    
    @Override
    public String toString() {
        return result.getName() + " = icmp " + condition.getCondStr() + " " + 
               operand1.toStringWithType() + ", " + operand2.toString();
    }
}
