package midend.symbol;

public enum SymbolType {
    CONST_INT("ConstInt"),
    INT("Int"),
    VOID_FUNC("VoidFunc"),
    INT_FUNC("IntFunc"),
    CONST_INT_ARRAY("ConstIntArray"),
    INT_ARRAY("IntArray"),
    STATIC_INT("StaticInt"),

    STATIC_INT_ARRAY("StaticIntArray"),
    FUNC_PARAM_INT,
    FUNC_PARAM_INT_ARRAY;

    private final String name;
    SymbolType(String name) { this.name = name; }
    SymbolType() { this.name = null; }
    public String getSymbolType() { return this.name; }
    public boolean isConst() {
        return this == CONST_INT || this == CONST_INT_ARRAY;
    }
    public boolean isArray() {
        return this == INT_ARRAY || this == CONST_INT_ARRAY || this == STATIC_INT_ARRAY || this == FUNC_PARAM_INT_ARRAY;
    }
    public boolean isFunc() {
        return this == INT_FUNC || this == VOID_FUNC;
    }
}
