package ast.factor;

import java.math.BigInteger;

import ast.Factor;
import ast.func.FuncRegistry;
import polynomial.Poly;
import polynomial.TermKey;

public class ConstFactor implements Factor {
    private final BigInteger value;

    public ConstFactor(BigInteger value) {
        this.value = value;
    }

    @Override
    public Factor reduce() {
        return this.clone();
    }

    @Override
    public Factor simplify() {
        return this.clone();
    }

    @Override
    public Factor derive(String var) {
        return new ConstFactor(BigInteger.ZERO);
    }

    @Override
    public Factor clone() {
        return new ConstFactor(value);
    }

    @Override
    public Factor substitute(Factor argument) {
        return this.clone();
    }

    @Override
    public Factor expandRec(FuncRegistry registry, String name, int currentN) {
        return this.clone();
    }

    @Override
    public Poly toPolynomial() {
        Poly p = new Poly();
        p.addTerm(new TermKey(), value);
        return p;
    }

    public BigInteger getValue() {
        return value;
    }
}
