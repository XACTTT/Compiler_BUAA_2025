package midend.symbol;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    private String name;
    private SymbolType type;
    private int scopeId;
    private int lineNum; // 声明的行号

    // 函数用
    private ArrayList<SymbolType> paramTypes = new ArrayList<>();
    private int dimension = 0; // 0: 非数组, 1: 一维, 2: 二维
    
    // IR生成用：常量值
    private Integer constValue = null; // 对于常量，存储其值
    
    // IR生成用：是否为全局变量
    private boolean isGlobal = false;
    
    // IR生成用：数组大小（一维数组）
    private Integer arraySize = null;

    public Symbol(String name, SymbolType type, int lineNum) {
        this.name = name;
        this.type = type;
        this.lineNum = lineNum;
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
    
    // IR生成相关getter/setter
    public Integer getConstValue() { return constValue; }
    public void setConstValue(Integer value) { this.constValue = value; }
    public boolean isGlobal() { return isGlobal; }
    public void setGlobal(boolean global) { this.isGlobal = global; }
    public Integer getArraySize() { return arraySize; }
    public void setArraySize(Integer size) { this.arraySize = size; }
    
    // 便捷方法
    public boolean isConstant() {
        return type.isConst();
    }
    
    public boolean isArray() {
        return type.isArray() || dimension > 0;
    }
    
    public boolean isFunction() {
        return type.isFunc();
    }

    // 可选: toString() 用于调试
    @Override
    public String toString() {
        return scopeId + " " + name + " " + (type.getSymbolType() != null ? type.getSymbolType() : type.name());
    }
}