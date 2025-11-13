package backend.llvm;

import java.util.ArrayList;

public class IRBasicBlock {
    private String label;
    private ArrayList<IRInstruction> instructions;
    
    public IRBasicBlock(String label) {
        this.label = label;
        this.instructions = new ArrayList<>();
    }
    
    public String getLabel() {
        return label;
    }
    
    public void addInstruction(IRInstruction inst) {
        instructions.add(inst);
    }
    
    public ArrayList<IRInstruction> getInstructions() {
        return instructions;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(label).append(":\n");
        for (IRInstruction inst : instructions) {
            sb.append("    ").append(inst.toString()).append("\n");
        }
        return sb.toString();
    }
}
