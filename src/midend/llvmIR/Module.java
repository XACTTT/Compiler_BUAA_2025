package midend.llvmIR;

import midend.llvmIR.value.Function;
import midend.llvmIR.value.GlobalVar;
import java.util.ArrayList;


public class Module {
    private ArrayList<GlobalVar> globalVariables = new ArrayList<>();
    private ArrayList<Function> functions = new ArrayList<>();

    public void addGlobalVar(GlobalVar globalVar) {
        globalVariables.add(globalVar);
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (GlobalVar globalVar : globalVariables) {
            sb.append(globalVar.toString()).append("\n");
        }

        for (Function function : functions) {
            sb.append(function.toString()).append("\n");
        }

        return sb.toString();
    }
}
