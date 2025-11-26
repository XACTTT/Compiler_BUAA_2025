package midend.llvmIR.type;
import java.util.ArrayList;

/**
 * FuncType - 函数类型
 * returnType: 返回值类型
 * paramTypes: 参数类型列表
 */
public class FuncType extends Type {
        private final Type returnType;
        private final ArrayList<Type> paramTypes;

        public FuncType(Type returnType, ArrayList<Type> paramTypes) {
            this.returnType = returnType;
            this.paramTypes = paramTypes;
        }

        public Type getReturnType() {
            return returnType;
        }

        public ArrayList<Type> getParamTypes() {
            return paramTypes;
        }
        
        // 函数类型的isVoid取决于返回值类型
        @Override
        public boolean isVoid() {
            return returnType != null && returnType.isVoid();
        }

        @Override
        public String toString() {
            return "";
        }

}
