package error;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyErrorHandler {
    private static class CompileError {
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
    }

    private static final MyErrorHandler instance = new MyErrorHandler();
    private final List<CompileError> errors = new ArrayList<>();

    private MyErrorHandler() {}

    public static MyErrorHandler getInstance() {
        return instance;
    }

    public void addError(int lineNumber, String errorCode) {
        errors.add(new CompileError(lineNumber, errorCode));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void printErrors(String filePath) throws IOException {
        errors.sort((e1, e2) -> Integer.compare(e1.lineNumber, e2.lineNumber));
        try (FileWriter writer = new FileWriter(filePath)) {
            for (CompileError error : errors) {
                writer.write(error.toString() + "\n");
            }
        }
    }
}
