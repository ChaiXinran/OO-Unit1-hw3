package ast.factor;

import ast.Factor;
import ast.func.FuncRegistry;
import polynomial.Poly;

public class FuncFactor implements Factor {
    private final Factor argument;
    private final FuncRegistry registry;
    private final String funcName;

    public FuncFactor(FuncRegistry registry, String funcName, Factor argument) {
        this.registry = registry;
        this.funcName = funcName;
        this.argument = argument;
    }

    @Override
    public Factor reduce() {
        return registry.evaluateFunc(funcName, argument);
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
        return new FuncFactor(registry, funcName, argument.clone());
    }

    @Override
    public Factor substitute(Factor arg) {
        Factor newArg = argument.substitute(arg);
        return new FuncFactor(registry, funcName, newArg);
    }

    @Override
    public Factor expandRec(FuncRegistry registry, String name, int currentN) {
        return new FuncFactor(registry, funcName, argument.expandRec(registry, name, currentN));
    }

    @Override
    public Poly toPolynomial() {
        return this.reduce().toPolynomial();
    }

    public FuncRegistry getRegistry() {
        return registry;
    }

    public String getFuncName() {
        return funcName;
    }

    public Factor getArgument() {
        return argument;
    }
}
