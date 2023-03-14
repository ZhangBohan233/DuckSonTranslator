package trashsoftware.duckSonTranslator.encrypt;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RSA {

    public RSA() {

    }

    public static void main(String[] args) {
        KeyPair keyPair = RSAKeyGenerator.getInstance().generate();
        System.out.println(keyPair);
        
        BigInteger[] enc = RSA.encrypt(
                keyPair.getPublicKey(),
                numToBigIntegers(new int[]{97, 98, 99}));
        System.out.println(Arrays.toString(enc));
        
        BigInteger[] dec = RSA.decrypt(
                keyPair.getPrivateKey(),
                enc
        );
        System.out.println(Arrays.toString(dec));
    }
    
    private static BigInteger[] numToBigIntegers(int[] ints) {
        BigInteger[] res = new BigInteger[ints.length];
        for (int i = 0 ; i< ints.length;i++) {
            res[i] = BigInteger.valueOf(ints[i]);
        }
        return res;
    }
    
    public static BigInteger encryptOne(PublicKey publicKey, BigInteger input) {
        return input.modPow(publicKey.getKey(), publicKey.getN());
    }

    public static BigInteger[] encrypt(PublicKey publicKey, 
                                       BigInteger[] input) {
        BigInteger[] res = new BigInteger[input.length];
        for (int i = 0; i < input.length; i++) {
            res[i] = input[i].modPow(publicKey.getKey(), publicKey.getN());
        }
        return res;
    }

    public static BigInteger decryptOne(PrivateKey privateKey, BigInteger input) {
        return input.modPow(privateKey.getKey(), privateKey.getN());
    }
    
    public static BigInteger[] decrypt(PrivateKey privateKey, 
                                       BigInteger[] encrypted) {
        BigInteger[] res = new BigInteger[encrypted.length];
        for (int i = 0; i < encrypted.length; i++) {
            res[i] = encrypted[i].modPow(privateKey.getKey(), privateKey.getN());
        }
        return res;
    }
}
