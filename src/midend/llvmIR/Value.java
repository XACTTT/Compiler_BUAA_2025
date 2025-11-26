package midend.llvmIR;

import midend.llvmIR.type.Type;

import java.util.ArrayList;

public class Value {
    protected String name;
    protected Type type;
    ArrayList<User> useList;
    
    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
        this.useList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }

    protected void addUse(User user) {
        useList.add(user);
    }
}