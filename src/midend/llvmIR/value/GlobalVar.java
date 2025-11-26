package midend.llvmIR.value;

import midend.llvmIR.User;
import midend.llvmIR.Value;
import midend.llvmIR.type.PointerType;
import midend.llvmIR.type.Type;
import midend.llvmIR.value.constant.Constant;

/**
 * GlobalVar - 全局变量
 * 类型为指针类型（指向实际数据）
 * operand[0]: 初始值常量
 * isConst: 是否为常量（决定输出global还是constant）
 */
public class GlobalVar extends User {
    private boolean isConst;

    public GlobalVar(String name, Constant initVal, boolean isConst) {
        super("@" + name, new PointerType(initVal.getType()));
        this.isConst = isConst;
        addOperand(initVal);
    }

    @Override
    public String toString() {
        // 格式: @name = dso_local global/constant i32 0
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = dso_local ");
        sb.append(isConst ? "constant " : "global ");

        Value initVal = getOperand(0);
        sb.append(initVal.getType()).append(" ");
        sb.append(initVal.toString());

        return sb.toString();
    }
}