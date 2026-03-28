package parser;

public class Token {
    private final TokenType type;
    private final String value;

    public enum TokenType {
        NUMBER,
        VARIABLE, // x/y
        DX, // dx
        DY, // dy
        GRAD, // grad
        POWER, // ^
        PLUS, // +
        MINUS, // -
        MULTIPLY, // *
        LPAREN, // (
        RPAREN, // )
        LBRACE, // {
        RBRACE, // }
        LBRACKET, // [
        RBRACKET, // ]
        EXP, // exp
        FUNC, // f
        EQUAL, // ==
        QUESTION, // ?
        COLON, // :
        EOF
    }

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
