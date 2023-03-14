package trashsoftware.duckSonTranslator.encrypt;

import java.math.BigInteger;

public class RSAKey {

    protected final BigInteger key;
    protected final BigInteger n;

    protected RSAKey(BigInteger key, BigInteger n) {
        this.key = key;
        this.n = n;
    }

    public BigInteger getKey() {
        return key;
    }

    public BigInteger getN() {
        return n;
    }

    @Override
    public String toString() {
        return "RSAKey{" +
                "key=" + key +
                ", n=" + n +
                '}';
    }
}
