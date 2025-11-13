package backend.llvm;

import java.util.ArrayList;

public class CallInst extends IRInstruction {
    private String funcName;
    private IRType returnType;
    private ArrayList<IRValue> args;
    
    public CallInst(IRValue result, IRType returnType, String funcName, ArrayList<IRValue> args) {
        super(result);
        this.returnType = returnType;
        this.funcName = funcName;
        this.args = args;
    }
    
    // For void functions
    public CallInst(IRType returnType, String funcName, ArrayList<IRValue> args) {
        this.returnType = returnType;
        this.funcName = funcName;
        this.args = args;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (result != null) {
            sb.append(result.getName()).append(" = ");
        }
        sb.append("call ").append(returnType).append(" @").append(funcName).append("(");
        for (int i = 0; i < args.size(); i++) {
            sb.append(args.get(i).toStringWithType());
            if (i < args.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
