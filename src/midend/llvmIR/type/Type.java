package midend.llvmIR.type;

/**
 * Type - LLVM类型系统的基类
 * 提供类型判断接口，子类重写对应方法返回true
 */
public abstract class Type {

    public abstract String toString();
    
    // 类型判断接口 - 用于指令生成时的类型检查
    public boolean isVoid() { return false; }
    public boolean isInteger1() { return false; }    // i1 布尔类型
    public boolean isInteger32() { return false; }   // i32 整数
    public boolean isPointer() { return false; }
    public boolean isArray() { return false; }
    public boolean isLabel() { return false; }       // BasicBlock标签类型
}
