package output;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import polynomial.Poly;
import polynomial.TermKey;

public class ExpOut {
    private static final int CACHE_LIMIT = 4096;
    private static final Map<Poly, String> EXP_FM_CACHE = new LinkedHashMap<Poly, String>(
            CACHE_LIMIT, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Poly, String> eldest) {
            return size() > CACHE_LIMIT;
        }
    };

    public static String formatExpFactor(Poly inner) {
        String cached = EXP_FM_CACHE.get(inner);
        if (cached != null) {
            return cached;
        }
        if (inner.isZero()) {
            return "1"; // Actually exp(0) is 1, should be handled in normalization
        }

        // Find GCD of all coefficients in inner
        BigInteger gcd = null;
        for (BigInteger c : inner.getTerms().values()) {
            BigInteger absC = c.abs();
            if (gcd == null) {
                gcd = absC;
            } else {
                gcd = gcd.gcd(absC);
            }
            if (gcd.equals(BigInteger.ONE)) {
                break;
            }
        }

        if (gcd == null || gcd.equals(BigInteger.ZERO)) {
            return "exp(0)";
        }

        int bestLength = Integer.MAX_VALUE;
        String bestStr = "";

        // 尝试：
        // 1. 完全外提 GCD，此时剩余在内部的就是 pulledPoly = inner / gcd, 外层指数为 gcd
        // 2. 对于 i 从 2 到 9，如果 gcd 能被 i 整除，则考虑“将 i 放回内部”，此时外层指数为 gcd / i，内部为 pulledPoly *
        // i
        // 这样外提的次数是 gcd / i
        // 3. 另外，还要加入直接完全不外提（外层指数 1，内部为 inner）作为保底候选项

        List<BigInteger> candOuterExps = new ArrayList<>();
        candOuterExps.add(BigInteger.ONE); // 不外提
        candOuterExps.add(gcd); // 完全外提

        for (int i = 2; i <= 9; i++) {
            BigInteger backIn = BigInteger.valueOf(i);
            if (gcd.remainder(backIn).equals(BigInteger.ZERO)) {
                // 如果 gcd 能被 i 整除，则可用的外提次数包括 gcd / i
                BigInteger outerExp = gcd.divide(backIn);
                if (!candOuterExps.contains(outerExp)) {
                    candOuterExps.add(outerExp);
                }
            }
        }

        for (BigInteger outerExp : candOuterExps) {
            // 对于选定的外层指数 outerExp，它一定是 gcd 的某个因数（1，gcd，或 gcd/i）
            // 内部多项式就是 inner / outerExp
            Poly pulledPoly = new Poly();
            for (Map.Entry<TermKey, BigInteger> e : inner.getTerms().entrySet()) {
                pulledPoly.addTerm(e.getKey(), e.getValue().divide(outerExp));
            }

            String innerStr = OutputEntry.formatCached(pulledPoly);
            String candStr;

            // Check if inner needs parenthesis
            if (OutputUtil.needsParen(pulledPoly)) {
                candStr = "exp((" + innerStr + "))";
            } else {
                candStr = "exp(" + innerStr + ")";
            }

            if (!outerExp.equals(BigInteger.ONE)) {
                candStr += "^" + outerExp;
            }

            if (candStr.length() < bestLength) {
                bestLength = candStr.length();
                bestStr = candStr;
            }
        }

        EXP_FM_CACHE.put(inner.clone(), bestStr);
        return bestStr;
    }
}
