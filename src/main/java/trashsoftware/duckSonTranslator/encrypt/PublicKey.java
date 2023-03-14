package trashsoftware.duckSonTranslator.encrypt;

import java.math.BigInteger;

public class PublicKey extends RSAKey {
    public PublicKey(BigInteger e, BigInteger n) {
        super(e, n);
    }
}
