package backend.llvm;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class IRModule {
    private ArrayList<String> declarations;
    private LinkedHashMap<String, StringInfo> globalStrings;
    private LinkedHashMap<String, IRValue> globalVariables;
    private ArrayList<IRFunction> functions;
    
    private static class StringInfo {
        String value;
        int size;
        
        StringInfo(String value, int size) {
            this.value = value;
            this.size = size;
        }
    }
    
    public IRModule() {
        this.declarations = new ArrayList<>();
        this.globalStrings = new LinkedHashMap<>();
        this.globalVariables = new LinkedHashMap<>();
        this.functions = new ArrayList<>();
        
        // Add standard library declarations
        declarations.add("declare i32 @getint()");
        declarations.add("declare void @putint(i32)");
        declarations.add("declare void @putch(i32)");
        declarations.add("declare void @putstr(i8*)");
    }
    
    public void addGlobalString(String name, String value, int size) {
        globalStrings.put(name, new StringInfo(value, size));
    }
    
    public void addGlobalVariable(String name, IRValue value) {
        globalVariables.put(name, value);
    }
    
    public void addFunction(IRFunction function) {
        functions.add(function);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Declarations
        for (String decl : declarations) {
            sb.append(decl).append("\n");
        }
        sb.append("\n");
        
        // Global strings
        for (String name : globalStrings.keySet()) {
            StringInfo info = globalStrings.get(name);
            sb.append(name).append(" = constant [")
              .append(info.size).append(" x i8] c\"")
              .append(info.value).append("\"\n");
        }
        if (!globalStrings.isEmpty()) {
            sb.append("\n");
        }
        
        // Global variables
        for (String name : globalVariables.keySet()) {
            IRValue value = globalVariables.get(name);
            sb.append(name).append(" = dso_local global ")
              .append(value.getType()).append(" ")
              .append(value.toString()).append("\n");
        }
        if (!globalVariables.isEmpty()) {
            sb.append("\n");
        }
        
        // Functions
        for (IRFunction func : functions) {
            sb.append(func.toString()).append("\n");
        }
        
        return sb.toString();
    }
}
