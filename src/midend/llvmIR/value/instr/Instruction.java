package midend.llvmIR.value.instr;


import midend.llvmIR.User;
import midend.llvmIR.Value;
import midend.llvmIR.type.*;
import midend.llvmIR.value.BasicBlock;
import midend.llvmIR.value.Function;

import java.util.ArrayList;

public abstract class Instruction extends User {
    private BasicBlock parent;

    public Instruction(Type type, String name, BasicBlock parent) {
        super(name, type);
        this.parent = parent;
        // 创建即插入：自动添加到基本块
        if (parent != null) {
            parent.addInstruction(this);
        }
    }

    public BasicBlock getParent() {
        return parent;
    }



    public static class BinaryInst extends Instruction {

        public enum Op {
            ADD("add"), SUB("sub"), MUL("mul"), SDIV("sdiv"), SREM("srem");
            private final String opName;
            Op(String name) { this.opName = name; }
            @Override public String toString() { return opName; }
        }

        private Op op;

        public BinaryInst(Op op, Value s1, Value s2, BasicBlock parent) {
            super(s1.getType(), "", parent);
            this.op = op;
            this.addOperand(s1);
            this.addOperand(s2);
        }

        @Override
        public String toString() {
            Value s1 = getOperand(0);
            Value s2 = getOperand(1);
            return String.format("%s = %s %s %s, %s",
                    getName(), op, s1.getType(), s1.getName(), s2.getName());
        }
    }


    public static class IcmpInst extends Instruction {
        public enum Cond {
            EQ("eq"), NE("ne"), SGT("sgt"), SGE("sge"), SLT("slt"), SLE("sle");
            private final String name;
            Cond(String name) { this.name = name; }
            @Override public String toString() { return name; }
        }

        private Cond cond;

        public IcmpInst(Cond cond, Value s1, Value s2, BasicBlock parent) {
            super(new IntegerType(1), "", parent);
            this.cond = cond;
            addOperand(s1);
            addOperand(s2);
        }

        @Override
        public String toString() {
            Value s1 = getOperand(0);
            Value s2 = getOperand(1);
            return String.format("%s = icmp %s %s %s, %s",
                    getName(), cond, s1.getType(), s1.getName(), s2.getName());
        }
    }


    public static class RetInst extends Instruction {

        public RetInst(Value val, BasicBlock parent) {
            super(new VoidType(), "", parent);
            addOperand(val);
        }

        public RetInst(BasicBlock parent) {
            super(new VoidType(), "", parent);
        }

        @Override
        public String toString() {
            if (operands.isEmpty()) {
                return "ret void";
            } else {
                Value retVal = getOperand(0);
                return "ret " + retVal.getType() + " " + retVal.getName();
            }
        }
    }


    public static class AllocaInst extends Instruction {
        private Type allocatedType;

        public AllocaInst(Type allocatedType, BasicBlock parent) {
            super(new PointerType(allocatedType), "", parent);
            this.allocatedType = allocatedType;
        }

        public Type getAllocatedType() {
            return allocatedType;
        }

        @Override
        public String toString() {
            return String.format("%s = alloca %s", getName(), allocatedType);
        }
    }


    public static class StoreInst extends Instruction {
        public StoreInst(Value value, Value pointer, BasicBlock parent) {
            super(new VoidType(), "", parent);
            addOperand(value);
            addOperand(pointer);
        }

        @Override
        public String toString() {
            Value val = getOperand(0);
            Value ptr = getOperand(1);
            return String.format("store %s %s, %s %s",
                    val.getType(), val.getName(), ptr.getType(), ptr.getName());
        }
    }


    public static class LoadInst extends Instruction {
        public LoadInst(Value pointer, BasicBlock parent) {
            super(((PointerType) pointer.getType()).getPointedType(), "", parent);
            addOperand(pointer);
        }

        @Override
        public String toString() {
            Value ptr = getOperand(0);
            return String.format("%s = load %s, %s %s",
                    getName(), getType(), ptr.getType(), ptr.getName());
        }
    }


    public static class BrInst extends Instruction {
        // 无条件跳转
        public BrInst(BasicBlock dest, BasicBlock parent) {
            super(new VoidType(), "", parent);
            addOperand(dest);
        }

        // 条件跳转
        public BrInst(Value cond, BasicBlock trueDest, BasicBlock falseDest, BasicBlock parent) {
            super(new VoidType(), "", parent);
            addOperand(cond);
            addOperand(trueDest);
            addOperand(falseDest);
        }

        @Override
        public String toString() {
            if (operands.size() == 1) {
                return "br label " + getOperand(0).getName();
            } else {
                return String.format("br i1 %s, label %s, label %s",
                        getOperand(0).getName(),
                        getOperand(1).getName(),
                        getOperand(2).getName());
            }
        }
    }


    public static class CallInst extends Instruction {
        public CallInst(Function func, ArrayList<Value> args, BasicBlock parent) {
            super(((FuncType)func.getType()).getReturnType(), "", parent);

            addOperand(func);
            if (args != null) {
                for (Value arg : args) {
                    addOperand(arg);
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            Function func = (Function) getOperand(0);

            if (!getType().isVoid()) {
                sb.append(getName()).append(" = ");
            }

            sb.append("call ").append(getType()).append(" ").append(func.getName()).append("(");

            for (int i = 1; i < operands.size(); i++) {
                Value arg = getOperand(i);
                sb.append(arg.getType()).append(" ").append(arg.getName());
                if (i < operands.size() - 1) sb.append(", ");
            }
            sb.append(")");
            return sb.toString();
        }
    }


    public static class ZextInst extends Instruction {
        public ZextInst(Value value, Type targetType, BasicBlock parent) {
            super(targetType, "", parent);
            addOperand(value);
        }

        @Override
        public String toString() {
            Value v = getOperand(0);
            return String.format("%s = zext %s %s to %s",
                    getName(), v.getType(), v.getName(), getType());
        }
    }


    public static class PhiInst extends Instruction {
        private final ArrayList<BasicBlock> incomingBlocks = new ArrayList<>();

        public PhiInst(Type type, BasicBlock parent) {
            super(type, "", parent);
        }

        public void addIncoming(Value value, BasicBlock block) {
            addOperand(value);
            incomingBlocks.add(block);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getName()).append(" = phi ").append(getType()).append(" ");
            for (int i = 0; i < operands.size(); i++) {
                if (i > 0) sb.append(", ");
                Value val = getOperand(i);
                BasicBlock block = incomingBlocks.get(i);
                sb.append("[ ").append(val.getName()).append(", ")
                        .append(block.getName()).append(" ]");
            }
            return sb.toString();
        }
    }


    public static class GetElementPtrInst extends Instruction {
        private ArrayList<Value> indices = new ArrayList<>();

        public GetElementPtrInst(Value basePointer, ArrayList<Value> idxs, BasicBlock parent) {
            super((basePointer.getType() instanceof PointerType && ((PointerType)basePointer.getType()).getPointedType() instanceof ArrayType)
                    ? new PointerType(((ArrayType)((PointerType)basePointer.getType()).getPointedType()).getElementType())
                    : basePointer.getType(), "", parent);
            addOperand(basePointer);
            if (idxs != null) {
                for (Value v : idxs) addOperand(v);
                this.indices.addAll(idxs);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getName()).append(" = getelementptr ");
            // base pointed type
            Value base = getOperand(0);
            Type pointed = ((PointerType) base.getType()).getPointedType();
            sb.append(pointed).append(", ");
            sb.append(base.getType()).append(" ").append(base.getName());

            for (int i = 1; i < operands.size(); i++) {
                Value idx = getOperand(i);
                sb.append(", i32 ").append(idx.getName());
            }
            return sb.toString();
        }
    }
}
