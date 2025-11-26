import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import frontend.Token;
import error.MyErrorHandler;
import frontend.ast.CompUnitNode;
import frontend.FrontEnd;
import frontend.ast.terminal.FuncTypeNode;
import frontend.ast.terminal.TokenNode;
import midend.MidEnd;

public class Compiler {
 public static void main(String[] args) {
  String inputFile = "testfile.txt";
  String outputFileError = "error.txt";
  String outputFileLexer = "lexer.txt";
  String outputFileParser = "parser.txt";
  String outputFileSymbol = "symbol.txt";

  MyErrorHandler errorHandler = MyErrorHandler.getInstance();
  try {
   String source = new String(Files.readAllBytes(Paths.get(inputFile)));
   FrontEnd frontend = new FrontEnd(source, errorHandler);
   frontend.GenerateTokenList();
   frontend.GenerateAstTree();
   MidEnd midEnd;
      midEnd = new MidEnd(frontend.getAstTree());
      midEnd.GenerateSymbolTable();
   if (errorHandler.hasErrors()) {
    errorHandler.printErrors(outputFileError);
    return;
   }
   try (FileWriter writer = new FileWriter(outputFileLexer)) {
    ArrayList<Token> tokens = frontend.getTokenList();
    for (Token token : tokens) {
     writer.write(token.toString() + "\n");
    }
   }

   try (FileWriter writer = new FileWriter(outputFileParser)) {
    CompUnitNode astRoot = frontend.getAstTree();
    if (astRoot != null) {
     writer.write(astRoot + "\n");
    }
   }
   try (FileWriter writer = new FileWriter(outputFileSymbol)) {
     writer.write(midEnd.GetSymbolTable().toString());
   }
   if (!errorHandler.hasErrors()) {
    // 1. 创建 IRBuilder
    midend.IRBuilder irBuilder = new midend.IRBuilder(midEnd.GetSymbolTable());

    // 2. 访问 AST 根节点，开始生成
    irBuilder.irVisit(frontend.getAstTree()); // 传入 CompUnitNode

    // 3. 输出到文件 llvm_ir.txt
    try (FileWriter writer = new FileWriter("llvm_ir.txt")) {
     writer.write(irBuilder.getModule().toString());
    }
   }
  } catch (IOException e) {
   System.err.println("错误：无法读取输入文件: " + e.getMessage());
  } catch (Exception e) {

   System.err.println("发生意外错误: " + e.getMessage());
   e.printStackTrace();
  }
 }
}


