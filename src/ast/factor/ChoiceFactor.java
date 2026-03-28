package ast.factor;

import java.math.BigInteger;

import ast.Factor;
import ast.func.FuncRegistry;
import polynomial.Poly;

// [(A == B) ? C : D]
public class ChoiceFactor implements Factor {
    private final Factor factorA;
    private final Factor factorB;
    private final Factor factorC;
    private final Factor factorD;

    public ChoiceFactor(Factor a, Factor b, Factor c, Factor d) {
        this.factorA = a;
        this.factorB = b;
        this.factorC = c;
        this.factorD = d;
    }

    @Override
    public Factor reduce() {
        Factor a = factorA.reduce();
        Factor b = factorB.reduce();
        Poly polyA = a.toPolynomial();
        Poly polyB = b.toPolynomial();
        if (polyA.equals(polyB)) {
            Factor ret = factorC.reduce();
            return ret == null ? new ConstFactor(BigInteger.ZERO) : ret;
        }
        Factor ret = factorD.reduce();
        return ret == null ? new ConstFactor(BigInteger.ZERO) : ret;
    }

    @Override
    public Factor simplify() {
        return this.clone();
    }

    @Override
    public Factor derive(String var) {
        return this.reduce().derive(var);
    }

    @Override
    public Factor clone() {
        return new ChoiceFactor(factorA.clone(), factorB.clone(), factorC.clone(), factorD.clone());
    }

    @Override
    public Factor substitute(Factor argument) {
        return new ChoiceFactor(
                factorA.substitute(argument),
                factorB.substitute(argument),
                factorC.substitute(argument),
                factorD.substitute(argument));
    }

    @Override
    public Factor expandRec(FuncRegistry registry, String name, int currentN) {
        return this.clone();
    }

    @Override
    public Poly toPolynomial() {
        return this.reduce().toPolynomial();
    }
}
