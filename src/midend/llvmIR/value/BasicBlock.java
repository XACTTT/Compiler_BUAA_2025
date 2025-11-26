package midend.llvmIR.value;

import midend.llvmIR.Value;
import midend.llvmIR.type.LabelType;
import midend.llvmIR.value.instr.Instruction;
import java.util.ArrayList;

/**
 * BasicBlock - 基本块（控制流图的节点）
 * 一个基本块是顺序执行的指令序列，只有一个入口和一个出口
 * parent: 所属函数
 * instructions: 指令列表
 * 构造时自动添加到父函数的块列表中
 */
public class BasicBlock extends Value {
    private Function parent;
    private ArrayList<Instruction> instructions = new ArrayList<>();

    public BasicBlock(String name, Function parent) {
        super("%" + name, new LabelType());
        this.parent = parent;
        if (parent != null) {
            parent.addBasicBlock(this);
        }
    }

    public Function getParent() {
        return parent;
    }

    public void addInstruction(Instruction inst) {
        instructions.add(inst);
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    // 判断块是否已终止（ret或br指令）- 用于死代码检测
    public boolean isTerminated() {
        if (instructions.isEmpty()) return false;
        Instruction last = instructions.get(instructions.size() - 1);
        return last instanceof Instruction.RetInst || last instanceof Instruction.BrInst;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 输出标签（去掉%前缀）: "entry:"
        sb.append(this.name.substring(1)).append(":\n");

        for (Instruction inst : instructions) {
            sb.append("  ").append(inst.toString()).append("\n");
        }
        return sb.toString();
    }
}