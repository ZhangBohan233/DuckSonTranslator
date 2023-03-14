package trashsoftware.duckSonTranslator.encrypt;

import java.math.BigInteger;

public class PrivateKey extends RSAKey {
    
    public PrivateKey(BigInteger d, BigInteger n) {
        super(d, n);
    }
}
