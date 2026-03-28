package ast;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ast.factor.ConstFactor;
import ast.func.FuncRegistry;
import polynomial.Poly;
import polynomial.TermKey;

public class Term implements Factor {
    private final ArrayList<Factor> factors;
    private boolean isNegative;

    public Term() {
        this.factors = new ArrayList<>();
        this.isNegative = false;
    }

    public void addFactor(Factor factor) {
        factors.add(factor);
    }

    public void setNegative(boolean negative) {
        this.isNegative = negative;
    }

    public boolean isNegative() {
        return isNegative;
    }

    public List<Factor> getFactors() {
        return new ArrayList<>(factors);
    }

    @Override
    public Factor reduce() {
        Term reduced = new Term();
        reduced.setNegative(this.isNegative);
        for (Factor factor : factors) {
            Factor rf = factor.reduce();
            Factor sf = rf.simplify();
            if (sf instanceof ConstFactor
                    && ((ConstFactor) sf).getValue().equals(BigInteger.ZERO)) {
                Term zeroTerm = new Term();
                zeroTerm.addFactor(new ConstFactor(BigInteger.ZERO));
                return zeroTerm;
            }
            if (sf instanceof ConstFactor && ((ConstFactor) sf).getValue().equals(BigInteger.ONE)) {
                continue;
            }
            reduced.addFactor(sf);
        }
        if (reduced.factors.isEmpty()) {
            reduced.addFactor(new ConstFactor(BigInteger.ONE));
        }
        return reduced;
    }

    @Override
    public Factor simplify() {
        if (factors.size() == 1 && factors.get(0) instanceof ConstFactor) {
            return this.clone();
        }
        return this.clone();
    }

    @Override
    public Factor derive(String var) {
        Expr resultExpr = new Expr();
        // 乘法法则：(uvw)' = u'vw + uv'w + uvw'
        for (int i = 0; i < factors.size(); i++) {
            Term term = new Term();
            term.setNegative(this.isNegative);
            for (int j = 0; j < factors.size(); j++) {
                if (i == j) {
                    term.addFactor(factors.get(j).derive(var));
                } else {
                    term.addFactor(factors.get(j).clone());
                }
            }
            resultExpr.addTerm(term);
        }
        return resultExpr;
    }

    @Override
    public Factor clone() {
        Term t = new Term();
        t.setNegative(this.isNegative);
        for (Factor factor : factors) {
            t.addFactor(factor.clone());
        }
        return t;
    }

    @Override
    public Factor substitute(Factor argument) {
        Term newTerm = new Term();
        newTerm.setNegative(this.isNegative);
        for (Factor f : factors) {
            newTerm.addFactor(f.substitute(argument));
        }
        return newTerm;
    }

    @Override
    public Factor expandRec(FuncRegistry registry, String name, int currentN) {
        Term newTerm = new Term();
        newTerm.setNegative(this.isNegative);
        for (Factor f : factors) {
            newTerm.addFactor(f.expandRec(registry, name, currentN));
        }
        return newTerm;
    }

    @Override
    public Poly toPolynomial() {
        Poly p = new Poly();
        p.addTerm(new TermKey(), BigInteger.ONE);
        for (Factor f : factors) {
            p = p.multiply(f.toPolynomial());
        }
        if (isNegative) {
            p = p.multiply(new Poly().addTermReturn(
                    new TermKey(), BigInteger.valueOf(-1)));
        }
        return p;
    }
}
