package error;

public class CompileError {
    int lineNumber;
    String errorCode;

    CompileError(int lineNumber, String errorCode) {
        this.lineNumber = lineNumber;
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return lineNumber + " " + errorCode;
    }

    public enum ErrorType {

        ILLEGAL_SYMBOL("a"),
        NAME_REDEFINITION("b"),
        UNDEFINED_NAME("c"),
        ARGUMENT_COUNT_MISMATCH("d"),
        ARGUMENT_TYPE_MISMATCH("e"),
        MISMATCHED_RETURN_IN_VOID_FUNCTION("f"),
        MISSING_RETURN("g"),
        MODIFY_CONSTANT("h"),
        MISSING_SEMICOLON("i"),
        MISSING_RIGHT_PARENTHESIS("j"),
        MISSING_RIGHT_BRACKET("k"),
        PRINTF_ARGUMENT_MISMATCH("l"),
        BREAK_CONTINUE_OUTSIDE_LOOP("m");
        private final String code;
        ErrorType(String code) {
            this.code = code;
        }


        public String getCode() {
            return code;
        }
    }
}
