package output;

import java.math.BigInteger;
import java.util.Map;

import polynomial.Poly;
import polynomial.TermKey;

final class OutputUtil {
    private OutputUtil() {
    }

    static boolean needsParen(Poly poly) {
        if (poly.getTerms().size() > 1) {
            return true;
        }
        if (poly.getTerms().size() == 1) {
            Map.Entry<TermKey, BigInteger> e = poly.getTerms().entrySet().iterator().next();
            TermKey k = e.getKey();
            boolean hasX = !k.getExpX().equals(BigInteger.ZERO);
            boolean hasY = !k.getExpY().equals(BigInteger.ZERO);
            boolean hasExp = k.getInner() != null && !k.getInner().isZero();
            boolean pureConst = !hasX && !hasY && !hasExp;
            if (pureConst) {
                return false;
            }
            if (e.getValue().compareTo(BigInteger.ZERO) < 0) {
                return true;
            }
            int factorCount = 0;
            boolean hasCoeff = !e.getValue().equals(BigInteger.ONE)
                    && !e.getValue().equals(BigInteger.valueOf(-1));
            if (hasCoeff) {
                factorCount++;
            }
            if (hasX) {
                factorCount++;
            }
            if (hasY) {
                factorCount++;
            }
            if (hasExp) {
                factorCount++;
            }
            return factorCount > 1;
        }
        return false;
    }
}
