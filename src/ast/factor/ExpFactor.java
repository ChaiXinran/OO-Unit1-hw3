package ast.factor;

import java.math.BigInteger;

import ast.Expr;
import ast.Factor;
import ast.Term;
import ast.func.FuncRegistry;
import polynomial.Poly;
import polynomial.TermKey;

public class ExpFactor implements Factor {
    private final Factor inner;
    private final BigInteger exponent;

    public ExpFactor(Factor inner, BigInteger exponent) {
        this.inner = inner;
        this.exponent = exponent;
    }

    public ExpFactor normalize() {
        if (exponent.equals(BigInteger.ONE)) {
            return this;
        }
        Term t = new Term();
        t.addFactor(new ConstFactor(exponent));
        t.addFactor(inner);
        Expr newInnerExpr = new Expr();
        newInnerExpr.addTerm(t);
        Factor reducedInner = newInnerExpr.reduce();
        return new ExpFactor(reducedInner, BigInteger.ONE);
    }

    @Override
    public Factor reduce() {
        if (exponent.equals(BigInteger.ZERO)) {
            return new ConstFactor(BigInteger.ONE);
        }
        Factor reducedInner = inner.reduce();
        if (reducedInner instanceof ConstFactor
                && ((ConstFactor) reducedInner).getValue().equals(BigInteger.ZERO)) {
            return new ConstFactor(BigInteger.ONE);
        }
        return new ExpFactor(reducedInner, exponent).normalize();
    }

    @Override
    public Factor simplify() {
        if (exponent.equals(BigInteger.ZERO)) {
            return new ConstFactor(BigInteger.ONE);
        }
        return this.clone();
    }

    @Override
    public Factor derive(String var) {
        ExpFactor normalized = (ExpFactor) this.reduce();
        Factor normalizedInner = normalized.inner;

        Term resultTerm = new Term();
        resultTerm.addFactor(new ExpFactor(normalizedInner, BigInteger.ONE));
        resultTerm.addFactor(normalizedInner.derive(var));

        Expr resultExpr = new Expr();
        resultExpr.addTerm(resultTerm);
        return resultExpr;
    }

    @Override
    public Factor clone() {
        return new ExpFactor(inner.clone(), exponent);
    }

    @Override
    public Factor substitute(Factor argument) {
        Factor newInner = inner.substitute(argument);
        return new ExpFactor(newInner, exponent).reduce();
    }

    @Override
    public Factor expandRec(FuncRegistry registry, String name, int currentN) {
        Factor newInner = inner.expandRec(registry, name, currentN);
        return new ExpFactor(newInner, exponent);
    }

    @Override
    public Poly toPolynomial() {
        ExpFactor normalized = (ExpFactor) this.normalize();
        Poly innerPoly = normalized.inner.toPolynomial();
        if (innerPoly.isZero()) {
            Poly p = new Poly();
            p.addTerm(new TermKey(), BigInteger.ONE);
            return p;
        }
        TermKey key = new TermKey(BigInteger.ZERO, BigInteger.ZERO, innerPoly);
        Poly p = new Poly();
        p.addTerm(key, BigInteger.ONE);
        return p;
    }

    public Factor getInner() {
        return inner;
    }

    public BigInteger getExponent() {
        return exponent;
    }
}
