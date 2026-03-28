package polynomial;

import java.math.BigInteger;
import java.util.Objects;

/**
 * 对应 a * x^b * y^c * exp(inner)^1 中的 x^b * y^c * exp(inner)^1 部分
 */
public final class TermKey implements Comparable<TermKey> {
    private final BigInteger expX;
    private final BigInteger expY;
    private final Poly inner; // 对于没有 exp 的项，inner 为 Poly.ZERO 或 null
    private final int hash;

    public TermKey(BigInteger expX, BigInteger expY, Poly inner) {
        this.expX = expX == null ? BigInteger.ZERO : expX;
        this.expY = expY == null ? BigInteger.ZERO : expY;
        this.inner = (inner == null) ? new Poly() : inner; // 默认给一个空(零)多项式
        this.hash = Objects.hash(this.expX, this.expY, this.inner);
    }

    public TermKey() {
        this(BigInteger.ZERO, BigInteger.ZERO, new Poly());
    }

    public BigInteger getExpX() {
        return expX;
    }

    public BigInteger getExpY() {
        return expY;
    }

    public Poly getInner() {
        return inner;
    }

    public TermKey multiply(TermKey other) {
        if (other == null) {
            return this;
        }
        BigInteger newExpX = this.expX.add(other.expX);
        BigInteger newExpY = this.expY.add(other.expY);
        Poly newInner = this.inner.add(other.inner); // exp(A)*exp(B) = exp(A+B)
        return new TermKey(newExpX, newExpY, newInner);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TermKey)) {
            return false;
        }
        TermKey o = (TermKey) obj;
        return this.expX.equals(o.expX) &&
                this.expY.equals(o.expY) &&
                this.inner.equals(o.inner);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public int compareTo(TermKey o) {
        // 先比 x 指数，后比 y 指数，最后比 inner。降幂排列，因此倒置比较
        int cmpX = o.expX.compareTo(this.expX);
        if (cmpX != 0) {
            return cmpX;
        }
        int cmpY = o.expY.compareTo(this.expY);
        if (cmpY != 0) {
            return cmpY;
        }
        return this.inner.compareTo(o.inner);
    }

    @Override
    public String toString() {
        return "x^" + expX + " * y^" + expY + " * exp(" + inner + ")";
    }
}
