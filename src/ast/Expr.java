package ast;

import java.util.ArrayList;
import java.util.List;

import ast.func.FuncRegistry;
import polynomial.Poly;

public class Expr implements Factor {
    private final ArrayList<Term> terms;

    public Expr() {
        this.terms = new ArrayList<>();
    }

    public void addTerm(Term term) {
        terms.add(term);
    }

    public List<Term> getTerms() {
        return new ArrayList<>(terms);
    }

    public static Expr mergeExpr(Expr expr1, Expr expr2) {
        if (expr1 == null) {
            return expr2;
        }
        if (expr2 == null) {
            return expr1;
        }
        Expr expr = new Expr();
        expr1.terms.forEach(expr::addTerm);
        expr2.terms.forEach(expr::addTerm);
        return expr;
    }

    @Override
    public Factor reduce() {
        Expr reduced = new Expr();
        for (Term term : terms) {
            Factor rt = term.reduce();
            if (rt instanceof Expr) {
                Expr re = (Expr) rt;
                for (Term t : re.getTerms()) {
                    reduced.addTerm((Term) t.clone());
                }
            } else if (rt instanceof Term) {
                reduced.addTerm((Term) rt);
            } else {
                Term t = new Term();
                t.addFactor(rt);
                reduced.addTerm(t);
            }
        }
        return reduced;
    }

    @Override
    public Factor simplify() {
        return this.clone();
    }

    @Override
    public Factor derive(String var) {
        Expr expr = new Expr();
        for (Term term : terms) {
            Factor derivedTerm = term.derive(var);
            if (derivedTerm instanceof Expr) {
                expr = mergeExpr(expr, (Expr) derivedTerm);
            } else if (derivedTerm instanceof Term) {
                expr.addTerm((Term) derivedTerm);
            } else {
                Term t = new Term();
                t.addFactor(derivedTerm);
                expr.addTerm(t);
            }
        }
        return expr;
    }

    @Override
    public Factor clone() {
        Expr e = new Expr();
        for (Term term : terms) {
            e.addTerm((Term) term.clone());
        }
        return e;
    }

    @Override
    public Factor substitute(Factor argument) {
        Expr newExpr = new Expr();
        for (Term t : terms) {
            newExpr.addTerm((Term) t.substitute(argument));
        }
        return newExpr;
    }

    @Override
    public Factor expandRec(FuncRegistry registry, String name, int currentN) {
        Expr newExpr = new Expr();
        for (Term t : terms) {
            newExpr.addTerm((Term) t.expandRec(registry, name, currentN));
        }
        return newExpr;
    }

    @Override
    public Poly toPolynomial() {
        Poly p = new Poly();
        for (Term t : terms) {
            p = p.add(t.toPolynomial());
        }
        return p;
    }
}
