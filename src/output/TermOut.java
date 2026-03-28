package output;

import java.math.BigInteger;

import polynomial.Poly;
import polynomial.TermKey;

public class TermOut {

    public static String formatTerm(TermKey key, BigInteger coeff) {
        StringBuilder sb = new StringBuilder();

        BigInteger expX = key.getExpX();
        BigInteger expY = key.getExpY();
        Poly inner = key.getInner();

        boolean hasVarsOrExp = !expX.equals(BigInteger.ZERO) ||
                !expY.equals(BigInteger.ZERO) ||
                (inner != null && !inner.isZero());

        if (coeff.equals(BigInteger.ZERO)) {
            return "0"; // Should not happen
        }

        if (coeff.equals(BigInteger.ONE) && hasVarsOrExp) {
            // coeff 1 omitted
        } else if (coeff.equals(BigInteger.valueOf(-1)) && hasVarsOrExp) {
            sb.append("-");
        } else {
            sb.append(coeff);
            if (hasVarsOrExp) {
                sb.append("*");
            }
        }

        boolean prevHasVar = false;
        if (!expX.equals(BigInteger.ZERO)) {
            sb.append("x");
            if (!expX.equals(BigInteger.ONE)) {
                sb.append("^").append(expX);
            }
            prevHasVar = true;
        }

        if (!expY.equals(BigInteger.ZERO)) {
            if (prevHasVar) {
                sb.append("*");
            }
            sb.append("y");
            if (!expY.equals(BigInteger.ONE)) {
                sb.append("^").append(expY);
            }
            prevHasVar = true;
        }

        if (inner != null && !inner.isZero()) {
            if (prevHasVar) {
                sb.append("*");
            }
            // Here we need to find the best representation for exp(inner)
            sb.append(ExpSplitter.formatAndSplit(inner));
        }

        return sb.toString();
    }
}
