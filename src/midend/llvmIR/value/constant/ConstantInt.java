package midend.llvmIR.value.constant;

import midend.llvmIR.type.IntegerType;

/**
 * ConstantInt - 整数常量
 * 直接输出数值，不需要额外标识符
 */
public class ConstantInt extends Constant {
    private int value;

    public ConstantInt(int value) {
        super(String.valueOf(value),new IntegerType(32));
        this.value = value;
    }

    public ConstantInt(int bitWidth, int value) {
        super(String.valueOf(value), new IntegerType(32));
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
