package midend.symbol;

import java.util.ArrayList;
import java.util.Comparator;

public class SymbolTable {
    private Scope root;
    private Scope currentNode; // 指向当前作用域
    private int nextScopeId = 1;

    private ArrayList<Symbol> orderedSymbols = new ArrayList<>();

    public SymbolTable() {
        root = new Scope(null, nextScopeId++);
        currentNode = root;
    }

    public void enterScope() {
        Scope newScope = new Scope(currentNode, nextScopeId++);
        currentNode.addChild(newScope);
        currentNode = newScope;
    }

    public void exitScope() {
        if (currentNode.getParent() != null) {
            currentNode = currentNode.getParent();
        }
    }

    // 错误 b
    public boolean checkRedefine(String name) {
        return currentNode.findSymbolCurrent(name) != null;
    }

    // 错误 c
    public Symbol findSymbol(String name) {
        return currentNode.findSymbolRecursive(name);
    }

    public void addSymbol(Symbol symbol) {
        currentNode.addSymbol(symbol);
        if (!(symbol.getName().equals("printf") || symbol.getName().equals("getint"))) {
            if (symbol.getType().getSymbolType() != null) {
                orderedSymbols.add(symbol);
            }
        }
    }

    public int getCurrentScopeId() {
        return currentNode.getScopeId();
    }

    // 获取排序符号表
    public ArrayList<Symbol> getSortedSymbols() {
        orderedSymbols.sort(Comparator.comparingInt(Symbol::getScopeId));
        return orderedSymbols;
    }

    public Scope getRoot() {
        return root;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Symbol> sortedSymbols = getSortedSymbols();
        for (int i = 0; i < sortedSymbols.size(); i++) {
            sb.append(sortedSymbols.get(i).toString());
            if (i < sortedSymbols.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}

