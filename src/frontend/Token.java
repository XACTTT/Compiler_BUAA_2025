package frontend;

public class Token {
    public  TokenType type;
    public  String value;
    public int lineNumber;

    public Token() {
        this.type = null;
        this.value = null;
        this.lineNumber = -1;
    }
    public TokenType getTokenType() {
        return type;
    }
    public String getValue() {
        return value;
    }
    enum TokenType{
        //标识符
        IDENFR,

        // 常量
        INTCON,STRCON,

        // 关键字
        CONSTTK,INTTK,STATICTK, BREAKTK, CONTINUETK, IFTK,
        MAINTK,ELSETK,FORTK,  RETURNTK,VOIDTK,PRINTFTK,

        // 算术运算符
        PLUS, MINU, MULT, DIV,MOD,

        //关系运算符
        LSS, LEQ, GRE, GEQ, EQL, NEQ,

        //逻辑运算符
        NOT, AND, OR,

        //分隔符
        SEMICN, COMMA, LPARENT, RPARENT, LBRACK, RBRACK,
        LBRACE,RBRACE,

        //赋值运算符
        ASSIGN,

        ERROR
    }

    public boolean isNull(){
        return type == null&&value == null;
    }

    @Override
    public String toString() {
        return this.type.toString()+" "+this.value;
    }
}
