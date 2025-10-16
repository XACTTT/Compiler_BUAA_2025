package frontend;

import java.util.ArrayList;
import java.util.HashMap;
import error.MyErrorHandler;
import error.CompileError;
public class Lexer {
    private final String source;
    private final ArrayList<Token> tokens = new ArrayList<>();
    private static final HashMap<String, Token.TokenType> keyWords = new HashMap<>();
    private int currentIndex = 0;
    private int currentLine = 1;

    static {
        keyWords.put("main", Token.TokenType.MAINTK);
        keyWords.put("const", Token.TokenType.CONSTTK);
        keyWords.put("int", Token.TokenType.INTTK);
        keyWords.put("break", Token.TokenType.BREAKTK);
        keyWords.put("continue", Token.TokenType.CONTINUETK);
        keyWords.put("if", Token.TokenType.IFTK);
        keyWords.put("else", Token.TokenType.ELSETK);
        keyWords.put("for", Token.TokenType.FORTK);
        keyWords.put("printf", Token.TokenType.PRINTFTK);
        keyWords.put("return", Token.TokenType.RETURNTK);
        keyWords.put("void", Token.TokenType.VOIDTK);
        keyWords.put("static", Token.TokenType.STATICTK);
    }

    MyErrorHandler errorHandler = MyErrorHandler.getInstance();
    public Lexer(String source) {
        this.source = source;
    }

    public ArrayList<Token> getTokens() {
        while (!isEnd()) {
            Token token = getNextToken();
            if (token != null && !token.isNull()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    public boolean isEnd() {
        return currentIndex >= source.length();
    }

    public Token getNextToken() {
        skipWhitespaceAndComments();
        if (isEnd()) {
            return null;
        }

        Token token = new Token();
        token.lineNumber = this.currentLine;
        char curChar = getCurChar();
        if (isIdentOrKeywords(curChar)) {
            //关键字和标识符
            getIdentOrKeywords(token);
        } else if (isDigitConst(curChar)) {
            //数字常量
            getDigit(token);
        } else if (isStrConst(curChar)) {
            //字符串常量
            getStrConst(token);
        } else {
            // 各种符号处理
            getSymbol(token);
        }

        return token;
    }

    private void getStrConst(Token token) {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(readChar());
        } while (!isEnd() && getCurChar() != '"');
        if (!isEnd()) {
            sb.append(readChar());
        }
        token.type = Token.TokenType.STRCON;
        token.value = sb.toString();
    }

    private boolean isStrConst(char curChar) {
        return curChar == '"';
    }

    //识别数字
    private void getDigit(Token token) {
        StringBuilder sb = new StringBuilder();
        while (!isEnd() && isDigitConst(getCurChar())) {
            sb.append(readChar());
        }
        token.type = Token.TokenType.INTCON;
        token.value = sb.toString();
    }

    private boolean isDigitConst(char curChar) {
        return curChar >= '0' && curChar <= '9';
    }

    private void getIdentOrKeywords(Token token) {
        StringBuilder sb = new StringBuilder();
        while (!isEnd() && (isIdentOrKeywords(getCurChar()) || isDigitConst(getCurChar()))) {
            sb.append(readChar());
        }
        String word = sb.toString();
        token.type = keyWords.getOrDefault(word, Token.TokenType.IDENFR);
        token.value = word;
    }

    private boolean isIdentOrKeywords(char curChar) {
        return (curChar >= 'a' && curChar <= 'z') ||
                (curChar >= 'A' && curChar <= 'Z') ||
                curChar == '_';
    }

    private void getSymbol(Token token) {
        char curChar = readChar();
        switch (curChar) {
            case '+': token.type = Token.TokenType.PLUS; token.value = "+"; break;
            case '-': token.type = Token.TokenType.MINU; token.value = "-"; break;
            case '*': token.type = Token.TokenType.MULT; token.value = "*"; break;
            case '%': token.type = Token.TokenType.MOD; token.value = "%"; break;
            case ';': token.type = Token.TokenType.SEMICN; token.value = ";"; break;
            case ',': token.type = Token.TokenType.COMMA; token.value = ","; break;
            case '(': token.type = Token.TokenType.LPARENT; token.value = "("; break;
            case ')': token.type = Token.TokenType.RPARENT; token.value = ")"; break;
            case '[': token.type = Token.TokenType.LBRACK; token.value = "["; break;
            case ']': token.type = Token.TokenType.RBRACK; token.value = "]"; break;
            case '{': token.type = Token.TokenType.LBRACE; token.value = "{"; break;
            case '}': token.type = Token.TokenType.RBRACE; token.value = "}"; break;

            case '!':
                if (!isEnd() && getCurChar() == '=') {
                    readChar();
                    token.type = Token.TokenType.NEQ; token.value = "!=";
                } else {
                    token.type = Token.TokenType.NOT; token.value = "!";
                }
                break;
            case '=':
                if (!isEnd() && getCurChar() == '=') {
                    readChar();
                    token.type = Token.TokenType.EQL; token.value = "==";
                } else {
                    token.type = Token.TokenType.ASSIGN; token.value = "=";
                }
                break;
            case '<':
                if (!isEnd() && getCurChar() == '=') {
                    readChar();
                    token.type = Token.TokenType.LEQ; token.value = "<=";
                } else {
                    token.type = Token.TokenType.LSS; token.value = "<";
                }
                break;
            case '>':
                if (!isEnd() && getCurChar() == '=') {
                    readChar();
                    token.type = Token.TokenType.GEQ; token.value = ">=";
                } else {
                    token.type = Token.TokenType.GRE; token.value = ">";
                }
                break;
            case '&':
                if (!isEnd() && getCurChar() == '&') {
                    readChar();
                    token.type = Token.TokenType.AND; token.value = "&&";
                } else
                {
                    token.type = Token.TokenType.AND; // “当作 && 进行处理”
                    token.value = "&";
                    errorHandler.addError(token.lineNumber,CompileError.ErrorType.ILLEGAL_SYMBOL);
                }
                    // 要记得错误处理
                break;
            case '|':
                if (!isEnd() && getCurChar() == '|') {
                    readChar();
                    token.type = Token.TokenType.OR; token.value = "||";
                } else
                {
                    token.type = Token.TokenType.OR;  // “当作 || 进行处理”
                    token.value = "|";
                    errorHandler.addError(token.lineNumber, CompileError.ErrorType.ILLEGAL_SYMBOL);
                }
                break;
            case '/':
                token.type = Token.TokenType.DIV; token.value = "/";
                break;
            default:
                // 未知符号
                token.type = Token.TokenType.ERROR; token.value = null; //
                break;
        }
    }

    private void skipWhitespaceAndComments() {
        while (!isEnd()) {
            char cur = getCurChar();
            if (cur == ' ' || cur == '\t' || cur == '\r' || cur == '\n') {//windows 是\n\r ，处理一下
                if (cur == '\n') {
                    currentLine++;
                }
                readChar();
                continue;
            }

            char next = (currentIndex + 1 < source.length()) ? source.charAt(currentIndex + 1) : '\0';
            if (cur == '/' && next == '/') {
                while (!isEnd() && getCurChar() != '\n') {
                    readChar();
                }
                if (!isEnd()) {
                    readChar();
                    currentLine++;
                }
                continue;
            } else if (cur == '/' && next == '*') {
                readChar();
                readChar();
                while (!isEnd()) {
                    if (getCurChar() == '*' && (currentIndex + 1 < source.length() && source.charAt(currentIndex + 1) == '/')) {
                        readChar();
                        readChar();
                        break;
                    }
                    if (getCurChar() == '\n') {
                        currentLine++;
                    }
                    readChar();
                }
                continue;
            }

            break;
        }
    }

    private char readChar() {
        return source.charAt(currentIndex++);
    }

    private char getCurChar() {
        return source.charAt(currentIndex);
    }
}