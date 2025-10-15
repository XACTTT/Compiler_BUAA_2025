import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import frontend.Lexer;
import frontend.Parser;
import frontend.Token;
import error.MyErrorHandler;
import frontend.ast.ASTnode;
import frontend.ast.CompUnitNode;

public class Compiler {
 public static void main(String[] args) {
  String inputFile = "testfile.txt";
  String outputFileLexer = "lexer.txt";
  String outputFileError = "error.txt";

  try {
   String source = new String(Files.readAllBytes(Paths.get(inputFile)));

   Lexer lexer = new Lexer(source);
   ArrayList<Token> tokens = lexer.getTokens();
   Parser parser = new Parser(tokens);
   CompUnitNode compUnitNode= parser.parseCompUnit();
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
