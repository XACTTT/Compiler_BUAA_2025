package midend.llvm;

import java.util.ArrayList;

public class IRFunction {
    private String name;
    private IRType returnType;
    private ArrayList<IRValue> params;
    private ArrayList<IRBasicBlock> basicBlocks;
    
    public IRFunction(String name, IRType returnType) {
        this.name = name;
        this.returnType = returnType;
        this.params = new ArrayList<>();
        this.basicBlocks = new ArrayList<>();
    }
    
    public void addParam(IRValue param) {
        params.add(param);
    }
    
    public void addBasicBlock(IRBasicBlock block) {
        basicBlocks.add(block);
    }
    
    public String getName() {
        return name;
    }
    
    public IRType getReturnType() {
        return returnType;
    }
    
    public ArrayList<IRBasicBlock> getBasicBlocks() {
        return basicBlocks;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define ").append(returnType).append(" @").append(name).append("(");
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).toStringWithType());
            if (i < params.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(") {\n");
        for (IRBasicBlock block : basicBlocks) {
            sb.append(block.toString());
        }
        sb.append("}\n");
        return sb.toString();
    }
}
