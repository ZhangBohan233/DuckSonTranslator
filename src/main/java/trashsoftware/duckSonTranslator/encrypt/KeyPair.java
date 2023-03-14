package trashsoftware.duckSonTranslator.encrypt;

import java.math.BigInteger;

public class KeyPair {
    
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    
    protected KeyPair(BigInteger e, BigInteger d, BigInteger n) {
        this.publicKey = new PublicKey(e, n);
        this.privateKey = new PrivateKey(d, n);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public String toString() {
        return "KeyPair{" +
                "publicKey=" + publicKey +
                ", privateKey=" + privateKey +
                '}';
    }
}
