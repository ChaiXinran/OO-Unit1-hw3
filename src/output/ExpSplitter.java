package output;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import polynomial.Poly;
import polynomial.TermKey;

public class ExpSplitter {
    private static final int CACHE_LIMIT = 4096;
    private static final Map<Poly, String> SP_CACHE = new LinkedHashMap<Poly, String>(
            CACHE_LIMIT, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Poly, String> eldest) {
            return size() > CACHE_LIMIT;
        }
    };

    public static String formatAndSplit(Poly inner) {
        if (inner.isZero()) {
            return "1";
        }
        String cached = SP_CACHE.get(inner);
        if (cached != null) {
            return cached;
        }
        String result = getBestSplit(inner);
        SP_CACHE.put(inner.clone(), result);
        return result;
    }

    private static String getBestSplit(Poly inner) {
        Set<BigInteger> candGcds = new HashSet<>();
        List<BigInteger> coeffs = new ArrayList<>();
        for (BigInteger c : inner.getTerms().values()) {
            BigInteger absC = c.abs();
            if (!absC.equals(BigInteger.ZERO)) {
                coeffs.add(absC);
            }
        }
        for (int i = 0; i < coeffs.size(); i++) {
            for (int j = i + 1; j < coeffs.size(); j++) {
                BigInteger g = coeffs.get(i).gcd(coeffs.get(j));
                if (g.compareTo(BigInteger.ONE) > 0) {
                    candGcds.add(g);
                }
            }
        }
        String bestStr = null;
        int bestLength = Integer.MAX_VALUE;

        for (BigInteger candGcd : candGcds) {
            String candStr = processCandGcd(inner, candGcd);
            if (candStr != null && candStr.length() < bestLength) {
                bestLength = candStr.length();
                bestStr = candStr;
            }
        }

        String fallback = ExpOut.formatExpFactor(inner);
        if (bestStr == null || fallback.length() < bestLength) {
            return fallback;
        }
        return bestStr;
    }

    private static String processCandGcd(Poly inner, BigInteger candGcd) {
        Poly polyPull = new Poly();
        Poly polyRest = new Poly();
        for (Map.Entry<TermKey, BigInteger> e : inner.getTerms().entrySet()) {
            if (e.getValue().remainder(candGcd).equals(BigInteger.ZERO)) {
                polyPull.addTerm(e.getKey(), e.getValue());
            } else {
                polyRest.addTerm(e.getKey(), e.getValue());
            }
        }
        if (polyPull.isZero() || polyRest.isZero()) {
            return null;
        }
        List<BigInteger> tryExps = new ArrayList<>();
        tryExps.add(candGcd);
        for (int i = 2; i <= 9; i++) {
            BigInteger bi = BigInteger.valueOf(i);
            if (candGcd.remainder(bi).equals(BigInteger.ZERO)) {
                tryExps.add(candGcd.divide(bi));
            }
        }
        String bestStr = null;
        int bestLength = Integer.MAX_VALUE;
        for (BigInteger outerExp : tryExps) {
            Poly polyPullDiv = new Poly();
            for (Map.Entry<TermKey, BigInteger> e : polyPull.getTerms().entrySet()) {
                polyPullDiv.addTerm(e.getKey(), e.getValue().divide(outerExp));
            }
            String stringRest = ExpOut.formatExpFactor(polyRest);
            String stringPullInner = OutputEntry.formatCached(polyPullDiv);
            String stringPull;
            if (OutputUtil.needsParen(polyPullDiv)) {
                stringPull = "exp((" + stringPullInner + "))";
            } else {
                stringPull = "exp(" + stringPullInner + ")";
            }
            if (!outerExp.equals(BigInteger.ONE)) {
                stringPull += "^" + outerExp;
            }
            String candStr = stringRest + "*" + stringPull;
            if (candStr.length() < bestLength) {
                bestLength = candStr.length();
                bestStr = candStr;
            }
        }
        return bestStr;
    }

}