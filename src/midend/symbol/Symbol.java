package midend.symbol;

import midend.llvmIR.Value;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    private String name;
    private SymbolType type;
    private int scopeId;
    private int lineNum; // 声明的行号
    private Value irValue;
    // 函数用
    private ArrayList<SymbolType> paramTypes = new ArrayList<>();
    private int dimension = 0; // 0: 非数组, 1: 一维
    private Integer constValue; // 标量常量的编译期值

    public Symbol(String name, SymbolType type, int lineNum) {
        this.name = name;
        this.type = type;
        this.lineNum = lineNum;
    }

    public void setIrValue(Value irValue) {
        this.irValue = irValue;
    }

    public Value getIrValue() {
        return irValue;
    }
    public String getName() { return name; }
    public SymbolType getType() { return type; }
    public int getScopeId() { return scopeId; }
    public void setScopeId(int scopeId) { this.scopeId = scopeId; }
    public int getLineNum() { return lineNum; }
    public ArrayList<SymbolType> getParamTypes() { return paramTypes; }
    public void addParamType(SymbolType paramType) { this.paramTypes.add(paramType); }
    public int getDimension() { return dimension; }
    public void setDimension(int dimension) { this.dimension = dimension; }
    public Integer getConstValue() { return constValue; }
    public void setConstValue(Integer constValue) { this.constValue = constValue; }

    // 可选: toString() 用于调试
    @Override
    public String toString() {
        return scopeId + " " + name + " " + (type.getSymbolType() != null ? type.getSymbolType() : type.name());
    }
}