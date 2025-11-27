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
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            sb.append(encodeChar(ch));
        }
        sb.append("\\00");
        return "c\"" + sb + "\"";
    }

    private String encodeChar(char ch) {
        switch (ch) {
            case '\n':
                return "\\0A";
            case '\t':
                return "\\09";
            case '\r':
                return "\\0D";
            case '"':
                return "\\22";
            case '\\':
                return "\\5C";
            default:
                if (ch >= 32 && ch <= 126) {
                    return Character.toString(ch);
                }
                return String.format("\\%02X", ch & 0xFF);
        }
    }
}