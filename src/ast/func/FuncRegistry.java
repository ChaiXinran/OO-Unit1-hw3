package ast.func;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ast.Expr;
import ast.Factor;
import ast.factor.ConstFactor;

public class FuncRegistry {
    private static final FuncRegistry INSTANCE = new FuncRegistry();

    private final Map<String, Expr> staticFuncs = new HashMap<>();

    // 递推函数使用，存储 f{0}(x), f{1}(x) 等
    private final Map<String, Expr> recBaseFuncs = new HashMap<>();
    // 递推函数的推导公式 f{n}(x)
    private final Map<String, RecDef> recRules = new HashMap<>();

    // 缓存：记录 "f_{n}_(toString(argument))" -> 已经化简好的 AST
    private final Map<String, Factor> recCache = new HashMap<>();

    public static FuncRegistry getInstance() {
        return INSTANCE;
    }

    public void registerStaticFunc(String name, Expr expr) {
        staticFuncs.put(name, (Expr) expr.reduce());
    }

    public Expr getStaticFunc(String name) {
        return staticFuncs.get(name);
    }

    public void registerRecBase(String name, int index, Expr expr) {
        recBaseFuncs.put(name + "{" + index + "}", (Expr) expr.reduce());
    }

    public void registerRecRule(String name, String ruleStr, Expr funcExpr) {
        RecDef rule = new RecDef();
        rule.setFuncExpr(funcExpr);
        recRules.put(name, rule);
    }

    // 提供给 FuncFactor 的求值接口
    public Factor evaluateFunc(String name, Factor argument) {
        Expr funcDef = staticFuncs.get(name);
        if (funcDef == null) {
            return new ConstFactor(BigInteger.ZERO);
        }
        if (isConstZero(funcDef)) {
            return new ConstFactor(BigInteger.ZERO);
        }
        return evaluateFuncReduced(name, argument.reduce());
    }

    public Factor evaluateFuncReduced(String name, Factor reducedArgument) {
        Expr funcDef = staticFuncs.get(name);
        if (funcDef == null) {
            return new ConstFactor(BigInteger.ZERO);
        }
        if (isConstZero(funcDef)) {
            return new ConstFactor(BigInteger.ZERO);
        }
        return funcDef.substitute(reducedArgument).reduce();
    }

    public Factor getRecFuncDef(String name, int n) {
        String cacheKey = name + "{" + n + "}";

        if (recCache.containsKey(cacheKey)) {
            return recCache.get(cacheKey).clone();
        }

        Factor result;
        if (n == 0 || n == 1) {
            result = recBaseFuncs.get(name + "{" + n + "}");
        } else if (n < 0) {
            result = new ConstFactor(BigInteger.ZERO);
        } else {
            RecDef rule = recRules.get(name);
            if (rule != null && rule.getFuncExpr() != null) {
                Expr ruleExpr = rule.getFuncExpr();
                result = ruleExpr.expandRec(this, name, n).reduce();
            } else {
                result = new ConstFactor(BigInteger.ZERO);
            }
        }

        recCache.put(cacheKey, result);
        return result.clone();
    }

    // 提供给 RecFuncFactor 的查表 / DP 接口
    public Factor evaluateRecFunc(String name, int n, Factor argument) {
        Factor def = getRecFuncDef(name, n);
        if (isConstZero(def)) {
            return new ConstFactor(BigInteger.ZERO);
        }
        Factor reducedArg = argument.reduce();
        return def.substitute(reducedArg).reduce();
    }

    public Factor evaluateRecFuncReduced(String name, int n, Factor reducedArg) {
        Factor def = getRecFuncDef(name, n);
        if (isConstZero(def)) {
            return new ConstFactor(BigInteger.ZERO);
        }
        return def.substitute(reducedArg).reduce();
    }

    private boolean isConstZero(Factor factor) {
        return factor instanceof ConstFactor
                && ((ConstFactor) factor).getValue().equals(BigInteger.ZERO);
    }

    public static class RecDef {
        private Expr funcExpr; // 额外的函数表达式

        public Expr getFuncExpr() {
            return funcExpr;
        }

        public void setFuncExpr(Expr funcExpr) {
            this.funcExpr = funcExpr;
        }
    }
}
