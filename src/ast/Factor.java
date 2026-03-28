package ast;

import ast.func.FuncRegistry;
import polynomial.Poly;

public interface Factor {

    Factor reduce();

    Factor simplify();

    Factor derive(String var);

    Factor clone();

    Factor substitute(Factor argument);

    Poly toPolynomial();

    Factor expandRec(FuncRegistry registry, String name, int currentN);
}
