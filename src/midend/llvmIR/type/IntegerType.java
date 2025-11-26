package midend.llvmIR.type;

/**
 * IntegerType - 整数类型 (i1, i32等)
 * bitWidth: 位宽，1表示布尔，32表示整数
 */
public class IntegerType extends Type {
    private final int bitWidth;

    public IntegerType(int bitWidth) {
        this.bitWidth = bitWidth;
    }

    @Override
    public String toString() {
        return "i" + bitWidth;
    }

    @Override
    public boolean isInteger1() {
        return bitWidth == 1;
    }

    @Override
    public boolean isInteger32() {
        return bitWidth == 32;
    }
}
