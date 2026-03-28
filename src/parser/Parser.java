package parser;

import java.math.BigInteger;

import ast.Expr;
import ast.Factor;
import ast.Term;
import ast.factor.ChoiceFactor;
import ast.factor.ConstFactor;
import ast.factor.DerivativeFactor;
import ast.factor.ExpFactor;
import ast.factor.ExprFactor;
import ast.factor.FuncFactor;
import ast.factor.RecFuncFactor;
import ast.factor.VarFactor;
import ast.func.FuncRegistry;

public class Parser {
    private final Lexer lexer;
    private FuncRegistry funcRegistry;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.funcRegistry = null;
    }

    public void setFuncRegistry(FuncRegistry registry) {
        this.funcRegistry = registry;
    }

    public static void parseAndRegisterFuncDef(String line, FuncRegistry registry) {
        String[] parts = line.split("=");
        String name = parts[0].trim().substring(0, 1);
        String exprStr = parts[1].trim();
        Lexer lexer = new Lexer(exprStr);
        Parser parser = new Parser(lexer);
        parser.setFuncRegistry(registry);
        Expr expr = parser.parseExpr();
        registry.registerStaticFunc(name, expr);
    }

    public static void parseAndRegisterRecDef(String line, FuncRegistry registry) {
        String[] parts = line.split("=");
        String left = parts[0].trim();
        String right = parts[1].trim();

        int braceStart = left.indexOf('{');
        int braceEnd = left.indexOf('}');
        String name = left.substring(0, braceStart);
        String idxStr = left.substring(braceStart + 1, braceEnd);

        if (idxStr.equals("0") || idxStr.equals("1")) {
            Lexer lexer = new Lexer(right);
            Parser parser = new Parser(lexer);
            parser.setFuncRegistry(registry);
            Expr expr = parser.parseExpr();
            registry.registerRecBase(name, Integer.parseInt(idxStr), expr);
        } else {
            Lexer lexer = new Lexer(right);
            Parser parser = new Parser(lexer);
            parser.setFuncRegistry(registry);
            Expr expr = parser.parseExpr();
            registry.registerRecRule(name, right, expr);
        }
    }

    // [+|-]? term ((+|-) term)*
    public Expr parseExpr() {
        Expr expr = new Expr();

        boolean isPositive = true;
        Token token = lexer.peek();
        if (token.getType() == Token.TokenType.PLUS) {
            lexer.next(); // consume '+'
            isPositive = true;
        } else if (token.getType() == Token.TokenType.MINUS) {
            lexer.next(); // consume '-'
            isPositive = false;
        }

        Term term = parseTerm();
        if (!isPositive) {
            term.setNegative(!term.isNegative());
        }
        expr.addTerm(term);

        while (lexer.peek().getType() == Token.TokenType.PLUS ||
                lexer.peek().getType() == Token.TokenType.MINUS) {
            isPositive = lexer.peek().getType() == Token.TokenType.PLUS;
            lexer.next(); // consume '+' or '-'
            term = parseTerm();
            if (!isPositive) {
                term.setNegative(!term.isNegative());
            }
            expr.addTerm(term);
        }

        return expr;
    }

    // [+|-]? factor ( * factor )*
    private Term parseTerm() {
        Term term = new Term();

        Token token = lexer.peek();
        if (token.getType() == Token.TokenType.PLUS) {
            lexer.next(); // consume '+'
        } else if (token.getType() == Token.TokenType.MINUS) {
            lexer.next(); // consume '-'
            term.setNegative(true);
        }

        Factor factor = parseFactor();
        term.addFactor(factor);

        while (lexer.peek().getType() == Token.TokenType.MULTIPLY) {
            lexer.next(); // consume '*'
            factor = parseFactor();
            term.addFactor(factor);
        }

        return term;
    }

    // NUMBER | (+|-)factor | dx()/dy()/grad() | VARIABLE[^exponent] |
    // (expr)[^exponent] | exp(factor)[^exponent] | f(...) | f{n}(...) | [choice]
    private Factor parseFactor() {
        Token token = lexer.peek();

        if (token.getType() == Token.TokenType.NUMBER) {
            return parseConstFactor();
        } else if (token.getType() == Token.TokenType.PLUS
                || token.getType() == Token.TokenType.MINUS) {
            return parseSignedConst();
        } else if (token.getType() == Token.TokenType.DX
                || token.getType() == Token.TokenType.DY
                || token.getType() == Token.TokenType.GRAD) {
            return parseDerivativeFactor();
        } else if (token.getType() == Token.TokenType.VARIABLE) {
            return parseVarFactor();
        } else if (token.getType() == Token.TokenType.LPAREN) {
            return parseExprFactor();
        } else if (token.getType() == Token.TokenType.EXP) {
            return parseExpFactor();
        } else if (token.getType() == Token.TokenType.FUNC) {
            return parseFuncCall();
        } else if (token.getType() == Token.TokenType.LBRACKET) {
            return parseChoiceFactor();
        }

        return null;
    }

    // NUMBER
    private Factor parseConstFactor() {
        Token token = lexer.peek();
        lexer.next(); // consume number
        BigInteger value = new BigInteger(token.getValue());
        return new ConstFactor(value);
    }

    // (+|-)NUMBER | (+|-)factor
    private Factor parseSignedConst() {
        Token token = lexer.peek();
        boolean isNegative = token.getType() == Token.TokenType.MINUS;
        lexer.next(); // consume '+' or '-'
        token = lexer.peek();
        if (token.getType() == Token.TokenType.NUMBER) {
            lexer.next(); // consume number
            BigInteger value = new BigInteger(token.getValue());
            if (isNegative) {
                value = value.negate();
            }
            return new ConstFactor(value);
        } else {
            Factor factor = parseFactor();
            if (isNegative) {
                Term term = new Term();
                term.setNegative(true);
                term.addFactor(factor);
                Expr expr = new Expr();
                expr.addTerm(term);
                return new ExprFactor(expr, BigInteger.ONE);
            }
            return factor;
        }
    }

    // dx(expr) | dy(expr) | grad(expr)
    private Factor parseDerivativeFactor() {
        Token token = lexer.peek();
        if (token.getType() == Token.TokenType.DX) {
            lexer.next(); // consume 'dx'
            lexer.next(); // consume '('
            Expr e = parseExpr();
            lexer.next(); // consume ')'
            return new DerivativeFactor(DerivativeFactor.Kind.DX, e);
        } else if (token.getType() == Token.TokenType.DY) {
            lexer.next(); // consume 'dy'
            lexer.next(); // consume '('
            Expr e = parseExpr();
            lexer.next(); // consume ')'
            return new DerivativeFactor(DerivativeFactor.Kind.DY, e);
        } else {
            // GRAD
            lexer.next(); // consume 'grad'
            lexer.next(); // consume '('
            Expr e = parseExpr();
            lexer.next(); // consume ')'
            return new DerivativeFactor(DerivativeFactor.Kind.GRAD, e);
        }
    }

    // VARIABLE[^exponent]
    private Factor parseVarFactor() {
        Token token = lexer.peek();
        String varName = token.getValue();
        lexer.next(); // consume variable
        BigInteger exponent = BigInteger.ONE;
        if (lexer.peek().getType() == Token.TokenType.POWER) {
            lexer.next(); // consume '^'
            exponent = parseExponent();
        }
        return new VarFactor(varName, exponent);
    }

    // (expr)[^exponent]
    private Factor parseExprFactor() {
        lexer.next(); // consume '('
        Expr expr = parseExpr();
        lexer.next(); // consume ')'
        BigInteger exponent = BigInteger.ONE;
        if (lexer.peek().getType() == Token.TokenType.POWER) {
            lexer.next(); // consume '^'
            exponent = parseExponent();
        }
        return new ExprFactor(expr, exponent);
    }

    // exp(<factor>)[^exponent]
    private Factor parseExpFactor() {
        lexer.next(); // consume 'exp'
        lexer.next(); // consume '('
        Factor inner = parseFactor();
        lexer.next(); // consume ')'
        BigInteger exponent = BigInteger.ONE;
        if (lexer.peek().getType() == Token.TokenType.POWER) {
            lexer.next(); // consume '^'
            exponent = parseExponent();
        }
        return new ExpFactor(inner, exponent);
    }

    // f(<factor>) | f{idx}(<factor>)
    private Factor parseFuncCall() {
        lexer.next(); // consume 'f'
        if (lexer.peek().getType() == Token.TokenType.LBRACE) {
            lexer.next(); // consume '{'
            // 根据指导书递推函数调用可能是 f{n-1} 或 f{n-2}，
            // 或者是单纯的 f{idx}。我们要看里面是什么
            String inside = "";
            Token cur = lexer.peek();
            while (cur.getType() != Token.TokenType.RBRACE
                    && cur.getType() != Token.TokenType.EOF) {
                // Lexer returns n as VARIABLE, 1/2 as NUMBER, minus as MINUS.
                if (cur.getType() == Token.TokenType.MINUS) {
                    inside += "-";
                } else if (cur.getType() == Token.TokenType.PLUS) {
                    inside += "+";
                } else {
                    inside += cur.getValue();
                }
                lexer.next();
                cur = lexer.peek();
            }
            lexer.next(); // consume '}'
            lexer.next(); // consume '('
            final Factor argument = parseFactor();
            lexer.next(); // consume ')'

            // 如果是 f{n-1} 或 f{n-2} 等，作为特殊变量存储
            if (inside.contains("n-1") || inside.contains("n-2") || inside.contains("n")) {
                // 这是一个带有相对下标的递推调用
                return new RecFuncFactor(funcRegistry, inside, argument);
            } else {
                // 这是带有绝对下标的递推调用 f{idx}
                final int idx = Integer.parseInt(inside);
                return new RecFuncFactor(funcRegistry, String.valueOf(idx), argument);
            }
        } else {
            lexer.next(); // consume '('
            final Factor argument = parseFactor();
            lexer.next(); // consume ')'
            return new FuncFactor(funcRegistry, "f", argument);
        }
    }

    // [(A == B) ? C : D]
    private Factor parseChoiceFactor() {
        lexer.next(); // consume '['
        lexer.next(); // consume '('
        final Factor a = parseExpr();
        lexer.next(); // consume '=='
        final Factor b = parseExpr();
        lexer.next(); // consume ')'
        lexer.next(); // consume '?'
        final Factor c = parseExpr();
        lexer.next(); // consume ':'
        final Factor d = parseExpr();
        lexer.next(); // consume ']'
        return new ChoiceFactor(a, b, c, d);
    }

    // [+|-]? number
    private BigInteger parseExponent() {
        Token token = lexer.peek();
        boolean isNegative = false;
        if (token.getType() == Token.TokenType.PLUS) {
            lexer.next(); // consume '+'
        } else if (token.getType() == Token.TokenType.MINUS) {
            lexer.next(); // consume '-'
            isNegative = true;
        }
        token = lexer.peek();
        lexer.next(); // consume number
        BigInteger value = new BigInteger(token.getValue());
        return isNegative ? value.negate() : value;
    }

}
