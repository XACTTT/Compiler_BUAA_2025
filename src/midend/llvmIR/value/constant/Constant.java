package midend.llvmIR.value.constant;

import midend.llvmIR.User;
import midend.llvmIR.type.Type;

/**
 * Constant - 常量基类
 * 常量的name直接是其字面值（如"10", "0"）
 */
public class Constant extends User {
    public Constant(String name, Type type) {
        super(name, type);
    }
}
