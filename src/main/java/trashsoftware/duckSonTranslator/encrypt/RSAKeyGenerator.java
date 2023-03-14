package trashsoftware.duckSonTranslator.encrypt;

import java.math.BigInteger;
import java.util.Random;

public class RSAKeyGenerator {
    
    private int bitsLow = 256;
    private int bitsHigh = 384;
    private int publicKeyBitsLow = 128;
    private int publicKeyBitsHigh = 192;
    private int nPrimesLow = bitsLow << 4;
    private int nPrimesHigh = bitsHigh << 5;

    private RSAKeyGenerator() {
    }

    public static void main(String[] args) {
        RSAKeyGenerator keyGenerator = getInstance();
        keyGenerator.setBits(2048, 3096);

        KeyPair keyPair = keyGenerator.generate();
        System.out.println(keyPair);
    }

    public static RSAKeyGenerator getInstance() {
        return new RSAKeyGenerator();
    }

    /**
     * Returns the greatest common divisor of two {@code BigInteger}s.
     * <p>
     * This method performs the classic Euclidean Algorithm.
     *
     * @param x a number
     * @param y another number
     * @return the greatest common divisor
     */
    public static BigInteger gcd(BigInteger x, BigInteger y) {
        BigInteger q, b;
        if (x.abs().compareTo(y.abs()) < 0) {  // a < b
            q = y;
            b = x;
        } else {
            q = x;
            b = y;
        }
        if (b.equals(BigInteger.ZERO)) return q;
        BigInteger[] dr;  // divisor and remainder
        while (!(dr = q.divideAndRemainder(b))[1].equals(BigInteger.ZERO)) {
            q = b;
            b = dr[1];
        }
        return b;
    }

    public static BigInteger lcm(BigInteger x, BigInteger y) {
        return x.multiply(y).divide(gcd(x, y));
    }

    public void setBits(int bits) {
        setBits(bits, bits + 1);
    }

    public void setBits(int bitsLow, int bitsHigh) {
        this.bitsLow = bitsLow;
        this.bitsHigh = bitsHigh;

        publicKeyBitsLow = bitsLow >> 1;
        if (publicKeyBitsLow < 2) {
            publicKeyBitsLow = 2;
        }
        publicKeyBitsHigh = publicKeyBitsLow * 3 / 2;
        nPrimesLow = bitsLow << 4;
        nPrimesHigh = bitsHigh << 4;
    }

    public KeyPair generate() {
        Random random = new Random();
        int pBits = random.nextInt(bitsHigh - bitsLow) + bitsLow;
        int qBits = random.nextInt(bitsHigh - bitsLow) + bitsLow;
        BigInteger p = BigInteger.probablePrime(pBits, random);
        BigInteger q = BigInteger.probablePrime(qBits, random);
        while (p.compareTo(q) == 0) {
            q = BigInteger.probablePrime(qBits, random);  // 确保p != q
        }

        BigInteger n = p.multiply(q);
        BigInteger phi = lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));
        BigInteger e = findEPrime(phi, random, p, q);
        BigInteger d = findD(phi, e);

        return new KeyPair(e, d, n);
    }
    
    private BigInteger findEPrime(BigInteger phi, Random rnd, BigInteger p, BigInteger q) {
        // 如果大数不是小数的倍数，且小数是质数，则一定互质
        // 确保生成的e不等于p和q
        BigInteger prime;
        while ((prime = nextRandomPrime(rnd)).compareTo(phi) < 0 && 
                prime.compareTo(p) != 0 && 
                prime.compareTo(q) != 0) {
            if (phi.remainder(prime).compareTo(BigInteger.ZERO) != 0) {
                return prime;
            }
        }
        throw new ArithmeticException("Cannot find a e");
    }
    
    private BigInteger nextRandomPrime(Random rnd) {
        int bits = rnd.nextInt(publicKeyBitsHigh - publicKeyBitsLow) + publicKeyBitsLow;
        return BigInteger.probablePrime(bits, rnd);
    }

    private BigInteger findEiter(BigInteger phi, Random rnd) {
        BigInteger tgt = BigInteger.probablePrime(publicKeyBitsLow, rnd);
        BigInteger res = null;
        int counter = 0;
        int nTh = rnd.nextInt(nPrimesHigh - nPrimesLow) + nPrimesLow;

        while (tgt.compareTo(phi) < 0) {
            if (gcd(tgt, phi).compareTo(BigInteger.ONE) == 0) {
                res = tgt;
                if (counter == nTh) break;
                counter++;
            }
            tgt = tgt.add(BigInteger.ONE);
        }
        if (res != null) return res;
        else throw new ArithmeticException("Not possible");
    }

    private BigInteger findD(BigInteger phi, BigInteger e) {
        // e * d mod phi == 1
        return e.modInverse(phi);
    }

//    private static Tuple extEuclid(BigInteger a, BigInteger b) {
//        if (b.compareTo(BigInteger.ZERO) == 0) {
//            return new Tuple(BigInteger.ONE, BigInteger.ZERO, a);
//        } else {
//            Tuple last = extEuclid(b, a.modInverse())
//        }
//    }

//    private static class Tuple {
//        private final BigInteger x;
//        private final BigInteger y;
//        private final BigInteger r;
//
//        Tuple(BigInteger x, BigInteger y, BigInteger r) {
//            this.x = x;
//            this.y = y;
//            this.r = r;
//        }
//    }
}
