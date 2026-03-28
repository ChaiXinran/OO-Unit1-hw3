package polynomial;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class Poly implements Comparable<Poly> {
    public static final Poly ZERO = new Poly();

    private final TreeMap<TermKey, BigInteger> terms;

    public Poly() {
        this.terms = new TreeMap<>();
    }

    public void addTerm(TermKey key, BigInteger coeff) {
        if (coeff.equals(BigInteger.ZERO)) {
            return;
        }
        BigInteger current = terms.getOrDefault(key, BigInteger.ZERO);
        BigInteger next = current.add(coeff);
        if (next.equals(BigInteger.ZERO)) {
            terms.remove(key);
        } else {
            terms.put(key, next);
        }
    }

    public Poly addTermReturn(TermKey key, BigInteger coeff) {
        Poly p = this.clone();
        p.addTerm(key, coeff);
        return p;
    }

    public Map<TermKey, BigInteger> getTerms() {
        return Collections.unmodifiableMap(terms);
    }

    public Poly add(Poly other) {
        if (other == null || other.isZero()) {
            return this;
        }
        if (this.isZero()) {
            return other;
        }
        Poly res = new Poly();
        this.terms.forEach(res::addTerm);
        other.terms.forEach(res::addTerm);
        return res;
    }

    public Poly multiply(Poly other) {
        if (this.isZero() || other == null || other.isZero()) {
            return new Poly();
        }
        Poly res = new Poly();
        for (Map.Entry<TermKey, BigInteger> e1 : this.terms.entrySet()) {
            for (Map.Entry<TermKey, BigInteger> e2 : other.terms.entrySet()) {
                TermKey newKey = e1.getKey().multiply(e2.getKey());
                BigInteger newCoeff = e1.getValue().multiply(e2.getValue());
                res.addTerm(newKey, newCoeff);
            }
        }
        return res;
    }

    public Poly pow(int exp) {
        if (exp == 0) {
            Poly p = new Poly();
            p.addTerm(new TermKey(), BigInteger.ONE);
            return p;
        }
        if (exp == 1) {
            return this;
        }
        Poly res = this;
        for (int i = 1; i < exp; i++) {
            res = res.multiply(this);
        }
        return res;
    }

    public boolean isZero() {
        return terms.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Poly)) {
            return false;
        }
        Poly o = (Poly) obj;
        return this.terms.equals(o.terms);
    }

    @Override
    public int hashCode() {
        return terms.hashCode();
    }

    @Override
    public int compareTo(Poly o) {
        if (this.terms.size() != o.terms.size()) {
            return Integer.compare(this.terms.size(), o.terms.size());
        }
        java.util.Iterator<Map.Entry<TermKey, BigInteger>> iter1 = this.terms.entrySet().iterator();
        java.util.Iterator<Map.Entry<TermKey, BigInteger>> iter2 = o.terms.entrySet().iterator();
        while (iter1.hasNext() && iter2.hasNext()) {
            Map.Entry<TermKey, BigInteger> e1 = iter1.next();
            Map.Entry<TermKey, BigInteger> e2 = iter2.next();
            int c = e1.getKey().compareTo(e2.getKey());
            if (c != 0) {
                return c;
            }
            c = e1.getValue().compareTo(e2.getValue());
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    public Poly clone() {
        Poly p = new Poly();
        this.terms.forEach(p::addTerm);
        return p;
    }

    @Override
    public String toString() {
        if (terms.isEmpty()) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<TermKey, BigInteger> entry : terms.entrySet()) {
            sb.append(entry.getValue()).append("*[").append(entry.getKey()).append("] + ");
        }
        return sb.substring(0, sb.length() - 3);
    }
}