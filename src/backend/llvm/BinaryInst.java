package backend.llvm;

public class BinaryInst extends IRInstruction {
    public enum OpType {
        ADD("add"),
        SUB("sub"),
        MUL("mul"),
        SDIV("sdiv"),
        SREM("srem");
        
        private String opStr;
        
        OpType(String opStr) {
            this.opStr = opStr;
        }
        
        public String getOpStr() {
            return opStr;
        }
    }
    
    private OpType op;
    private IRValue operand1;
    private IRValue operand2;
    
    public BinaryInst(IRValue result, OpType op, IRValue operand1, IRValue operand2) {
        super(result);
        this.op = op;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }
    
    @Override
    public String toString() {
        return result.getName() + " = " + op.getOpStr() + " " + 
               operand1.toStringWithType() + ", " + operand2.toString();
    }
}
