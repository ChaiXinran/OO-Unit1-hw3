package ast.factor;

import java.math.BigInteger;

import ast.Expr;
import ast.Factor;
import ast.Term;
import ast.func.FuncRegistry;
import polynomial.Poly;
import polynomial.TermKey;

public class VarFactor implements Factor {
    private final String varName;
    private final BigInteger exponent;

    public VarFactor(String varName, BigInteger exponent) {
        this.varName = varName;
        this.exponent = exponent;
    }

    @Override
    public Factor reduce() {
        return this.simplify();
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
        if (!this.varName.equals(var)) {
            return new ConstFactor(BigInteger.ZERO);
        }
        if (exponent == null || exponent.equals(BigInteger.ZERO)) {
            return new ConstFactor(BigInteger.ZERO);
        }
        if (exponent.equals(BigInteger.ONE)) {
            return new ConstFactor(BigInteger.ONE);
        }
        BigInteger exp = exponent;
        BigInteger newExp = exp.subtract(BigInteger.ONE);
        Term t = new Term();
        t.addFactor(new ConstFactor(exp));
        t.addFactor(new VarFactor(varName, newExp));
        Expr result = new Expr();
        result.addTerm(t);
        return result;
    }

    @Override
    public Factor clone() {
        return new VarFactor(varName, exponent);
    }

    @Override
    public Factor substitute(Factor argument) {
        if (!this.varName.equals("x")) {
            return this.clone();
        }
        if (this.exponent.equals(BigInteger.ZERO)) {
            return new ConstFactor(BigInteger.ONE);
        } else if (this.exponent.equals(BigInteger.ONE)) {
            return argument.clone();
        } else {
            if (argument instanceof Expr) {
                return new ExprFactor((Expr) argument.clone(), this.exponent).reduce();
            }
            Expr expr = new Expr();
            if (argument instanceof Term) {
                expr.addTerm((Term) argument.clone());
            } else {
                Term t = new Term();
                t.addFactor(argument.clone());
                expr.addTerm(t);
            }
            return new ExprFactor(expr, this.exponent).reduce();
        }
    }

    @Override
    public Factor expandRec(FuncRegistry registry, String name, int currentN) {
        return this.clone();
    }

    @Override
    public Poly toPolynomial() {
        Factor simplified = this.simplify();
        if (simplified instanceof VarFactor) {
            VarFactor vf = (VarFactor) simplified;
            TermKey key;
            if (vf.varName.equals("x")) {
                key = new TermKey(vf.exponent, BigInteger.ZERO, null);
            } else {
                key = new TermKey(BigInteger.ZERO, vf.exponent, null);
            }
            Poly p = new Poly();
            p.addTerm(key, BigInteger.ONE);
            return p;
        }
        return simplified.toPolynomial();
    }

    public String getVarName() {
        return varName;
    }

    public BigInteger getExponent() {
        return exponent;
    }
}
