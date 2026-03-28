package ast.factor;

import java.math.BigInteger;

import ast.Expr;
import ast.Factor;
import ast.Term;
import ast.func.FuncRegistry;
import polynomial.Poly;

// dx(expr), dy(expr) or grad(expr)
public class DerivativeFactor implements Factor {
    public enum Kind {
        DX, DY, GRAD
    }

    private final Kind kind;
    private final Expr expr;

    public DerivativeFactor(Kind kind, Expr expr) {
        this.kind = kind;
        this.expr = expr;
    }

    @Override
    public Factor reduce() {
        Factor reducedExpr = expr.reduce();
        Factor derived;
        switch (kind) {
            case DX:
                derived = reducedExpr.derive("x");
                break;
            case DY:
                derived = reducedExpr.derive("y");
                break;
            case GRAD:
                Factor dx = reducedExpr.derive("x").reduce();
                Factor dy = reducedExpr.derive("y").reduce();
                Term termDx = new Term();
                termDx.addFactor(dx);
                Term termDy = new Term();
                termDy.addFactor(dy);
                Expr gradExpr = new Expr();
                gradExpr.addTerm(termDx);
                gradExpr.addTerm(termDy);
                derived = gradExpr;
                break;
            default:
                derived = new ConstFactor(BigInteger.ZERO);
                break;
        }
        return derived.reduce();
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
        return new DerivativeFactor(kind, (Expr) expr.clone());
    }

    @Override
    public Factor substitute(Factor argument) {
        return this.clone();
    }

    @Override
    public Factor expandRec(FuncRegistry registry, String name, int currentN) {
        return new DerivativeFactor(kind, (Expr) expr.expandRec(registry, name, currentN));
    }

    @Override
    public Poly toPolynomial() {
        return this.reduce().toPolynomial();
    }

    public Kind getKind() {
        return kind;
    }

    public Expr getExpr() {
        return expr;
    }
}
