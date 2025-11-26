package midend.llvmIR;

import midend.llvmIR.type.Type;
import java.util.ArrayList;


public class User extends Value {
    protected ArrayList<Value> operands = new ArrayList<>();

    public User(String name, Type type) {
        super(name, type);
    }

    // 添加操作数并建立双向use-def关系
    public void addOperand(Value v) {
        operands.add(v);
        v.addUse(this);
    }

    public Value getOperand(int index) {
        return operands.get(index);
    }

    public int getNumOperands() {
        return operands.size();
    }
}