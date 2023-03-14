package trashsoftware.duckSonTranslator.encrypt;

import trashsoftware.duckSonTranslator.dict.Util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class LiteralRSAUtil {
    static final BigInteger ANDER = BigInteger.valueOf(63);
    static final char[] ZERO_POOL = {'*', '#', '%', '$'};
    
    static final BigInteger SIXTY_ONE = BigInteger.valueOf(61);
    
    static final int KEY_EACH_PART_LENGTH = 5;

    public static String keyToLiteral(RSAKey key) {
        return keyToLiteral(key, false);
    }
    
    public static String keyToLiteral(RSAKey key, boolean readable) {
        String keyS = numToSerializeString(key.getKey());
        String nS = numToSerializeString(key.getN());
        
        String full = keyS + "+" + nS;
        if (!readable) return full;
        
        int count = 0;
        StringBuilder builder = new StringBuilder();
        for (char c : full.toCharArray()) {
            builder.append(c);
            count++;
            if (count % KEY_EACH_PART_LENGTH == 0) builder.append(' ');
        }
        while (count % KEY_EACH_PART_LENGTH != 0) {
            builder.append('Z');
            count++;
        }
        return builder.toString();
    }
    
    public static PublicKey literalToPublicKey(String literal) {
        BigInteger[] en = literalToKey(literal);
        return new PublicKey(en[0], en[1]);
    }

    public static PrivateKey literalToPrivateKey(String literal) {
        BigInteger[] dn = literalToKey(literal);
        return new PrivateKey(dn[0], dn[1]);
    }
    
    static BigInteger[] literalToKey(String literal) {
        String replaced = literal.replace("Z", "");
        replaced = replaced.replace(" ", "");
        String[] split = replaced.split("\\+");
        if (split.length != 2) throw new InvalidLiteralException("Not a key literal", true);
        
        return new BigInteger[]{
                serializeStringToNum(split[0]),
                serializeStringToNum(split[1])
        };
    }
    
    static BigInteger bytesToBigInt(byte[] bytes, int index, EncodeSpec spec) {
        BigInteger res = BigInteger.ZERO;
        for (int i = index; i < index + spec.getInChunkByteLength(); i++) {
            res = res.shiftLeft(8);
            if (i < bytes.length) {
                res = res.or(BigInteger.valueOf(bytes[i] & 0xff));
            }
        }
        return res;
    }

    static byte[] bigIntToBytes(BigInteger num, EncodeSpec spec) {
        byte[] res = new byte[spec.getInChunkByteLength()];
        for (int i = 0; i < res.length; i++) {
            res[res.length - i - 1] = (byte) num.intValue();
            num = num.shiftRight(8);
        }
        return res;
    }

    /**
     * 转换为6位的明文字母表。
     * <p>
     * 明文字母表包含64个元素:
     * 星/井/百分/钱(1)
     * 点(1)
     * 大写字母(26)
     * 小写字母(26)
     * 数字(10)
     */
    static String bigIntToFunnyString(BigInteger bigInteger, EncodeSpec spec) {
        char[] res = new char[spec.getChunkCharCount()];
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < spec.getChunkCharCount(); i++) {
            int shift = (spec.getChunkCharCount() - i - 1) * 6;
            int bits = bigInteger.shiftRight(shift).and(ANDER).intValue();
            res[i] = intToFunny(bits);
        }
        return new String(res);
    }

    static char intToFunny(int num) {
        if (num == 0) return ZERO_POOL[(int) (Math.random() * ZERO_POOL.length)];
        if (num < 27) return (char) (num - 1 + 'a');
        if (num < 53) return (char) (num - 27 + 'A');
        if (num < 63) return (char) (num - 53 + '0');
        if (num == 63) return '.';
        throw new RuntimeException("Cannot encode");
    }

    static int funnyToInt(char funny) {
        if (funny == '.') return 63;
        if (Util.arrayContains(ZERO_POOL, funny)) return 0;
        if (funny <= 'z') {
            if (funny >= 'a') return funny - 'a' + 1;
            if (funny <= 'Z') {
                if (funny >= 'A') return funny - 'A' + 27;
                if (funny <= '9') {
                    if (funny >= '0') return funny - '0' + 53;
                }
            }
        }
        throw new RuntimeException("Cannot decode");
    }

    /**
     * 不会有Z
     */
    static char numToChar61(int num) {
        if (num < 10) return (char) (num + '0');
        if (num < 36) return (char) (num - 10 + 'a');
        if (num < 61) return (char) (num - 36 + 'A');
        throw new RuntimeException("Cannot encode");
    }

    static int char61ToNum(char c) {
        if (c <= 'z') {
            if (c >= 'a') return c - 'a' + 10;
            if (c <= 'Y') {
                if (c >= 'A') return c - 'A' + 36;
                if (c <= '9') {
                    if (c >= '0') return c - '0';
                }
            }
        }
        throw new InvalidLiteralException(String.valueOf(c));
    }
    
    static String numToSerializeString(BigInteger num) {
        StringBuilder builder = new StringBuilder();
        
        while (num.compareTo(BigInteger.ZERO) != 0) {
            BigInteger[] divRem = num.divideAndRemainder(SIXTY_ONE);
            builder.append(numToChar61(divRem[1].intValue()));
            num = divRem[0];
        }
        
        builder.reverse();
        return builder.toString();
    }
    
    static BigInteger serializeStringToNum(String string) {
        BigInteger result = BigInteger.ZERO;
        for (char c : string.toCharArray()) {
            if (c != ' ' && c != 'Z') {
                result = result.multiply(SIXTY_ONE);
                result = result.add(BigInteger.valueOf(char61ToNum(c)));
            }
        }
        
        return result;
    }
}
