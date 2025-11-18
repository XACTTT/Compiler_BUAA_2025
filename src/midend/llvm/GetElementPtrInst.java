package midend.llvm;

import java.util.ArrayList;

public class GetElementPtrInst extends IRInstruction {
    private IRType baseType;
    private IRValue pointer;
    private ArrayList<IRValue> indices;
    
    public GetElementPtrInst(IRValue result, IRType baseType, IRValue pointer, ArrayList<IRValue> indices) {
        super(result);
        this.baseType = baseType;
        this.pointer = pointer;
        this.indices = indices;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(result.getName()).append(" = getelementptr ");
        sb.append(baseType).append(", ");
        sb.append(pointer.toStringWithType());
        for (IRValue index : indices) {
            sb.append(", ").append(index.toStringWithType());
        }
        return sb.toString();
    }
}
