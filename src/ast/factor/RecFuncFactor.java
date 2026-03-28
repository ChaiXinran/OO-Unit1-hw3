package ast.factor;

import ast.Factor;
import ast.func.FuncRegistry;
import polynomial.Poly;

public class RecFuncFactor implements Factor {
    private final FuncRegistry registry;
    private final String indexStr; // e.g. "n-1", "0", "1"
    private final Factor argument;

    public RecFuncFactor(FuncRegistry registry, String indexStr, Factor argument) {
        this.registry = registry;
        this.indexStr = indexStr;
        this.argument = argument;
    }

    public RecFuncFactor(FuncRegistry registry, int index, Factor argument) {
        this(registry, String.valueOf(index), argument);
    }

    @Override
    public Factor reduce() {
        if (indexStr.contains("n")) {
            return this.clone();
        }
        return registry.evaluateRecFunc(
                getFuncName(), Integer.parseInt(indexStr), argument);
    }

    @Override
    public Factor simplify() {
        return this.clone();
    }

    private String getFuncName() {
        return "f";
    }

    @Override
    public Factor derive(String var) {
        return this.reduce().derive(var);
    }

    @Override
    public Factor clone() {
        return new RecFuncFactor(registry, indexStr, argument.clone());
    }

    @Override
    public Factor substitute(Factor arg) {
        Factor newArg = argument.substitute(arg);
        return new RecFuncFactor(registry, indexStr, newArg).reduce();
    }

    @Override
    public Factor expandRec(FuncRegistry registry, String name, int currentN) {
        int newIdx = currentN;
        if (indexStr.equals("n-1")) {
            newIdx = currentN - 1;
        } else if (indexStr.equals("n-2")) {
            newIdx = currentN - 2;
        } else if (indexStr.equals("n")) {
            newIdx = currentN;
        } else {
            newIdx = Integer.parseInt(indexStr);
        }
        return registry.getRecFuncDef(name, newIdx)
                .substitute(argument.expandRec(registry, name, currentN)).reduce();
    }

    @Override
    public Poly toPolynomial() {
        if (indexStr.contains("n")) {
            return new Poly();
        }
        return this.reduce().toPolynomial();
    }

    public String getIndexStr() {
        return indexStr;
    }

    public FuncRegistry getRegistry() {
        return registry;
    }

    public Factor getArgument() {
        return argument;
    }
}