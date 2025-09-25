import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import frontend.Lexer;
import frontend.Token;
import error.MyErrorHandler;

public class Compiler {
 public static void main(String[] args) {
  String inputFile = "testfile.txt";
  String outputFileLexer = "lexer.txt";
  String outputFileError = "error.txt";

  try {
   String source = new String(Files.readAllBytes(Paths.get(inputFile)));

   Lexer lexer = new Lexer(source);
   List<Token> tokens = lexer.getTokens();

   MyErrorHandler errorHandler = MyErrorHandler.getInstance();
   if (errorHandler.hasErrors()) {
    errorHandler.printErrors(outputFileError);
   } else {
    try (FileWriter writer = new FileWriter(outputFileLexer)) {
     for (Token token : tokens) {
      writer.write(token.toString() + "\n");
     }
    }
   }

  } catch (IOException e) {
   System.err.println("Error processing files: " + e.getMessage());
  }
 }
}
