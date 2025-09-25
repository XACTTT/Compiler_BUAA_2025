package error;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MyErrorHandler {

    private static final MyErrorHandler instance = new MyErrorHandler();
    private final List<CompileError> errors = new ArrayList<>();

    private MyErrorHandler() {
    }

    public static MyErrorHandler getInstance() {
        return instance;
    }

    public void addError(int lineNumber, CompileError.ErrorType errorType) {
        errors.add(new CompileError(lineNumber, errorType.getCode() ));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void printErrors(String filePath) throws IOException {
        errors.sort(Comparator.comparingInt(e -> e.lineNumber));
        try (FileWriter writer = new FileWriter(filePath)) {
            for (CompileError error : errors) {
                writer.write(error.toString() + "\n");
            }
        }
    }
}
