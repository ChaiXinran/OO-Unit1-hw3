package ast.factor;

import java.math.BigInteger;

import ast.Expr;
import ast.Factor;
import ast.Term;
import ast.func.FuncRegistry;
import polynomial.Poly;

public class ExprFactor implements Factor {
    private final Expr expr;
    private final BigInteger exponent;

    public ExprFactor(Expr expr, BigInteger exponent) {
        this.expr = expr;
        this.exponent = exponent;
    }

    @Override
    public Factor reduce() {
        if (exponent.equals(BigInteger.ZERO)) {
            return new ConstFactor(BigInteger.ONE);
        }
        Factor reducedExpr = expr.reduce();
        if (exponent.equals(BigInteger.ONE)) {
            return reducedExpr;
        }
        if (reducedExpr instanceof Expr) {
            return new ExprFactor((Expr) reducedExpr, exponent);
        }
        Expr wrapped = new Expr();
        if (reducedExpr instanceof Term) {
            wrapped.addTerm((Term) reducedExpr);
        } else {
            Term t = new Term();
            t.addFactor(reducedExpr);
            wrapped.addTerm(t);
        }
        return new ExprFactor(wrapped, exponent);
    }

    @Override
    public Factor simplify() {
        if (exponent.equals(BigInteger.ZERO)) {
            return new ConstFactor(BigInteger.ONE);
        }
        if (exponent.equals(BigInteger.ONE)) {
            return expr.clone();
        }
        return this.clone();
    }

    @Override
    public Factor derive(String var) {
        if (exponent.equals(BigInteger.ZERO)) {
            return new ConstFactor(BigInteger.ZERO);
        }
        // (E^n)' = n * E^(n-1) * E'
        Expr innerDeriv = (Expr) expr.derive(var);

        Term resultTerm = new Term();
        resultTerm.addFactor(new ConstFactor(exponent));
        if (exponent.compareTo(BigInteger.ONE) > 0) {
            resultTerm.addFactor(new ExprFactor(expr, exponent.subtract(BigInteger.ONE)));
        }
        resultTerm.addFactor(new ExprFactor(innerDeriv, BigInteger.ONE));

        Expr result = new Expr();
        result.addTerm(resultTerm);
        return result;
    }

    @Override
    public Factor clone() {
        return new ExprFactor((Expr) expr.clone(), exponent);
    }

    @Override
    public Factor substitute(Factor argument) {
        Factor newExpr = expr.substitute(argument);
        if (newExpr instanceof Expr) {
            return new ExprFactor((Expr) newExpr, exponent).reduce();
        }
        Expr e = new Expr();
        if (newExpr instanceof Term) {
            e.addTerm((Term) newExpr);
        } else {
            Term t = new Term();
            t.addFactor(newExpr);
            e.addTerm(t);
        }
        return new ExprFactor(e, exponent).reduce();
    }

    @Override
    public Factor expandRec(FuncRegistry registry, String name, int currentN) {
        Factor newExpr = expr.expandRec(registry, name, currentN);
        if (newExpr instanceof Expr) {
            return new ExprFactor((Expr) newExpr, exponent);
        }
        Expr e = new Expr();
        if (newExpr instanceof Term) {
            e.addTerm((Term) newExpr);
        } else {
            Term t = new Term();
            t.addFactor(newExpr);
            e.addTerm(t);
        }
        return new ExprFactor(e, exponent);
    }

    @Override
    public Poly toPolynomial() {
        if (exponent.equals(BigInteger.ZERO)) {
            Poly p = new Poly();
            p.addTerm(new polynomial.TermKey(), BigInteger.ONE);
            return p;
        }
        Poly basePoly = expr.toPolynomial();
        if (exponent.equals(BigInteger.ONE)) {
            return basePoly;
        }
        return basePoly.pow(exponent.intValue());
    }

    public Expr getExpr() {
        return expr;
    }

    public BigInteger getExponent() {
        return exponent;
    }
}
