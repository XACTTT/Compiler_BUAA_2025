package midend.llvmIR.value;

import midend.llvmIR.Value;
import midend.llvmIR.type.Type;
import midend.llvmIR.value.Function;

/**
 * Argument - 函数形参
 * 在函数定义中作为参数，命名为%a0, %a1等
 */
public class Argument extends Value {
    private Function parent;

    public Argument(String name, Type type, Function parent) {
        super(name, type);
        this.parent = parent;
    }
}