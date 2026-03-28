package output;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import polynomial.Poly;
import polynomial.TermKey;

public class OutputEntry {
    private static final int CACHE_LIMIT = 4096;
    private static final Map<Poly, String> FM_CACHE = new LinkedHashMap<Poly, String>(
            CACHE_LIMIT, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Poly, String> eldest) {
            return size() > CACHE_LIMIT;
        }
    };

    static String formatCached(Poly poly) {
        String cached = FM_CACHE.get(poly);
        if (cached != null) {
            return cached;
        }
        String value = format(poly);
        FM_CACHE.put(poly.clone(), value);
        return value;
    }

    public static String format(Poly poly) {
        if (poly == null || poly.isZero()) {
            return "0";
        }

        List<Map.Entry<TermKey, BigInteger>> posTerms = new ArrayList<>();
        List<Map.Entry<TermKey, BigInteger>> negTerms = new ArrayList<>();

        for (Map.Entry<TermKey, BigInteger> entry : poly.getTerms().entrySet()) {
            if (entry.getValue().compareTo(BigInteger.ZERO) > 0) {
                posTerms.add(entry);
            } else {
                negTerms.add(entry);
            }
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Map.Entry<TermKey, BigInteger> entry : posTerms) {
            if (!first) {
                sb.append("+");
            }
            sb.append(TermOut.formatTerm(entry.getKey(), entry.getValue()));
            first = false;
        }

        for (Map.Entry<TermKey, BigInteger> entry : negTerms) {
            String termStr = TermOut.formatTerm(entry.getKey(), entry.getValue());
            if (first) {
                sb.append(termStr);
                first = false;
            } else {
                if (termStr.startsWith("-")) {
                    sb.append(termStr);
                } else {
                    sb.append("-").append(termStr);
                }
            }
        }

        return sb.toString();
    }
}
