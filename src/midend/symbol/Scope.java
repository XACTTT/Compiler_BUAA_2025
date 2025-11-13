package midend.symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scope {
    private Map<String, Symbol> symbols = new HashMap<>();
    private Scope parent;
    private List<Scope> children = new ArrayList<>();
    private int scopeId;

    public Scope(Scope parent, int scopeId) {
        this.parent = parent;
        this.scopeId = scopeId;
    }

    public int getScopeId() { return scopeId; }
    public Scope getParent() { return parent; }
    public List<Scope> getChildren() { return children; }
    public void addChild(Scope child) { children.add(child); }

    public boolean addSymbol(Symbol symbol) {
        if (symbols.containsKey(symbol.getName())) {
            return false; // 重定义
        }
        symbols.put(symbol.getName(), symbol);
        symbol.setScopeId(this.scopeId); // 设置归属
        return true;
    }

    public Symbol findSymbolCurrent(String name) {
        return symbols.get(name);
    }

    public Symbol findSymbolRecursive(String name) {
        Scope current = this;
        while (current != null) {
            Symbol symbol = current.symbols.get(name);
            if (symbol != null) {
                return symbol;
            }
            current = current.parent;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Scope " + scopeId + " symbols: " + symbols.keySet();
    }
}