package trashsoftware.duckSonTranslator.encrypt;

import java.math.BigInteger;

public class RSAAttacker {

    public static void main(String[] args) {
        RSAKeyGenerator generator = RSAKeyGenerator.getInstance();
        generator.setBits(32);
        KeyPair keyPair = generator.generate();
        System.out.println(keyPair);

        long st = System.currentTimeMillis();
        
        PrivateKey privateKey = computePrivateKey(keyPair.getPublicKey());
        
        long end = System.currentTimeMillis();

        System.out.println("Cracked key: " + privateKey);
        System.out.println("time used: " + (end - st));
    }
    
    public static PrivateKey computePrivateKey(PublicKey publicKey) {
        BigInteger n = publicKey.getN();
        BigInteger low = BigInteger.valueOf(2);
        BigInteger p = null;
        
        BigInteger cur = n.sqrt();
        while (cur.compareTo(low) > 0) {
            BigInteger rem = n.remainder(cur);
            if (rem.compareTo(BigInteger.ZERO) == 0) {
                p = cur;
                break;
            }
            cur = cur.subtract(BigInteger.ONE);
        }
        if (p == null) throw new RuntimeException();
        BigInteger q = n.divide(p);
        BigInteger phi = RSAKeyGenerator.lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));
        BigInteger d = publicKey.getKey().modInverse(phi);
        return new PrivateKey(d, n);
    }
}
