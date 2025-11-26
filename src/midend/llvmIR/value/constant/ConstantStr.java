package midend.llvmIR.value.constant;

import midend.llvmIR.type.ArrayType;
import midend.llvmIR.type.IntegerType;

public class ConstantStr extends Constant {
    private String content;

    public ConstantStr(String content) {
        // 类型是 [n x i8]，其中 n 是字符串长度 + 1 (包含 \0)
        super("", new ArrayType(new IntegerType(8), content.length() + 1));
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        // 输出格式: c"Hello World\00"
        // 需要处理转义字符，简单起见先把 \n 替换为 \0A
        String printContent = content.replace("\n", "\\0A") + "\\00";
        return "c\"" + printContent + "\"";
    }
}