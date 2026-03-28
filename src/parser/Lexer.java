package parser;

public class Lexer {
    private final String input;
    private int pos;
    private Token curToken;

    public Lexer(String input) {
        this.input = input;
        this.pos = 0;
        next();
    }

    public void next() {
        if (pos >= input.length()) {
            curToken = new Token(Token.TokenType.EOF, "");
            return;
        }

        if (parseMultiChar()) {
            return;
        }

        if (Character.isDigit(input.charAt(pos))) {
            curToken = new Token(Token.TokenType.NUMBER, getNumber());
            return;
        }

        parseSingleChar();
    }

    private boolean parseMultiChar() {
        if (startsWith("dx")) {
            curToken = new Token(Token.TokenType.DX, "dx");
            pos += 2;
            return true;
        } else if (startsWith("dy")) {
            curToken = new Token(Token.TokenType.DY, "dy");
            pos += 2;
            return true;
        } else if (startsWith("grad")) {
            curToken = new Token(Token.TokenType.GRAD, "grad");
            pos += 4;
            return true;
        } else if (startsWith("exp")) {
            curToken = new Token(Token.TokenType.EXP, "exp");
            pos += 3;
            return true;
        } else if (startsWith("==")) {
            curToken = new Token(Token.TokenType.EQUAL, "==");
            pos += 2;
            return true;
        }
        return false;
    }

    private void parseSingleChar() {
        char c = input.charAt(pos);
        switch (c) {
            case 'x':
                curToken = new Token(Token.TokenType.VARIABLE, "x");
                break;
            case 'y':
                curToken = new Token(Token.TokenType.VARIABLE, "y");
                break;
            case 'n':
                curToken = new Token(Token.TokenType.VARIABLE, "n");
                break;
            case 'f':
                curToken = new Token(Token.TokenType.FUNC, "f");
                break;
            case '^':
                curToken = new Token(Token.TokenType.POWER, "^");
                break;
            case '+':
                curToken = new Token(Token.TokenType.PLUS, "+");
                break;
            case '-':
                curToken = new Token(Token.TokenType.MINUS, "-");
                break;
            case '*':
                curToken = new Token(Token.TokenType.MULTIPLY, "*");
                break;
            case '(':
                curToken = new Token(Token.TokenType.LPAREN, "(");
                break;
            case ')':
                curToken = new Token(Token.TokenType.RPAREN, ")");
                break;
            case '{':
                curToken = new Token(Token.TokenType.LBRACE, "{");
                break;
            case '}':
                curToken = new Token(Token.TokenType.RBRACE, "}");
                break;
            case '[':
                curToken = new Token(Token.TokenType.LBRACKET, "[");
                break;
            case ']':
                curToken = new Token(Token.TokenType.RBRACKET, "]");
                break;
            case '?':
                curToken = new Token(Token.TokenType.QUESTION, "?");
                break;
            case ':':
                curToken = new Token(Token.TokenType.COLON, ":");
                break;
            default:
                pos++;
                next();
                return; // already handled
        }
        pos++;
    }

    private boolean startsWith(String s) {
        return input.startsWith(s, pos);
    }

    public Token peek() {
        return curToken;
    }

    private String getNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }
        return sb.toString();
    }
}
