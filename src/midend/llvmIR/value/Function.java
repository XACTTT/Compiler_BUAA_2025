package midend.llvmIR.value;

import midend.llvmIR.User;
import midend.llvmIR.type.FuncType;
import midend.llvmIR.type.Type;
import java.util.ArrayList;

/**
 * Function - 函数定义/声明
 * blocks: 基本块列表（函数体）
 * arguments: 形参列表
 * isLibrary: 是否为库函数（库函数用declare，用户函数用define）
 */
public class Function extends User {
    private ArrayList<BasicBlock> blocks = new ArrayList<>();
    private ArrayList<Argument> arguments = new ArrayList<>();
    private boolean isLibrary = false;

    public Function(String name, FuncType type, boolean isLibrary) {
        super("@" + name, type);
        this.isLibrary = isLibrary;
    }

    public void addBasicBlock(BasicBlock block) {
        blocks.add(block);
    }

    public ArrayList<BasicBlock> getBlocks() {
        return blocks;
    }

    public ArrayList<Argument> getArguments() {
        return arguments;
    }

    public void addArgument(Argument arg) {
        arguments.add(arg);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // 库函数用declare，用户函数用define
        String prefix = isLibrary ? "declare " : "define ";

        FuncType ft = (FuncType) this.type;
        sb.append(prefix).append(ft.getReturnType()).append(" ").append(this.name).append("(");

        // 参数列表: i32 %a0, i32 %a1
        for (int i = 0; i < arguments.size(); i++) {
            sb.append(arguments.get(i).getType()).append(" ").append(arguments.get(i).getName());
            if (i < arguments.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append(")");

        if (isLibrary) {
            sb.append("\n");
        } else {
            sb.append(" {\n");
            for (BasicBlock block : blocks) {
                sb.append(block.toString());
            }
            sb.append("}\n");
        }

        return sb.toString();
    }
}
