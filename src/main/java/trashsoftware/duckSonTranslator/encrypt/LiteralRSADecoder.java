package trashsoftware.duckSonTranslator.encrypt;

import trashsoftware.duckSonTranslator.encrypt.literalConverter.LiteralConverter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class LiteralRSADecoder {

    private final PrivateKey privateKey;
    private final LiteralConverter converter;

    public LiteralRSADecoder(PrivateKey privateKey, LiteralConverter converter) {
        this.privateKey = privateKey;
        this.converter = converter;
    }

//    private static BigInteger recoverFromFunny(PointedString funnyStr, EncodeSpec spec) {
//        BigInteger res = BigInteger.ZERO;
//
//        for (int i = 0; i < spec.getChunkCharCount(); i++) {
//            char c = funnyStr.nextNonSpaceChar();
//            int recoveredBits = LiteralRSAUtil.funnyToInt(c);
//            res = res.shiftLeft(6);
//            res = res.or(BigInteger.valueOf(recoveredBits));
//        }
//        return res;
//    }

    private String[] splitEncString(String enc) {
        String replaced = enc.replace(" ", "");
        return replaced.split(String.valueOf(converter.getSplitChar()));
    }

    public String decode(String encText) {
        EncodeSpec spec = new EncodeSpec(privateKey);

        String[] splitText = splitEncString(encText);

        List<byte[]> result = new ArrayList<>();
        int resultLength = 0;
        int bufferLength = 0;
        for (String numT : splitText) {
            BigInteger next = converter.stringToNum(numT);
            BigInteger dec = RSA.decryptOne(privateKey, next);
            byte[] chunk = LiteralRSAUtil.bigIntToBytes(dec, spec);
            bufferLength += chunk.length;
            for (int i = 0; i < chunk.length; i++) {
                if (chunk[i] == 0) {
                    for (int j = i + 1; j < chunk.length; j++) {
                        if (chunk[j] != 0) {
                            throw new RuntimeException("Should not follow after null terminator");
                        }
                    }
                    break;
                } else resultLength++;
            }
            result.add(chunk);
        }
        byte[] resultBytes = new byte[bufferLength];
        int i = 0;
        for (byte[] bytes : result) {
            System.arraycopy(bytes, 0, resultBytes, i, bytes.length);
            i += bytes.length;
        }

        return new String(resultBytes, 0, resultLength);

//        PointedString funnyStr = new PointedString(encText);

//        List<byte[]> result = new ArrayList<>();
//        int resultLength = 0;
//        int bufferLength = 0;
//        while (funnyStr.hasNextNonSpaceChar()) {
//            BigInteger next = recoverFromFunny(funnyStr, spec);
//            BigInteger dec = RSA.decryptOne(privateKey, next);
//            byte[] chunk = LiteralRSAUtil.bigIntToBytes(dec, spec);
//            bufferLength += chunk.length;
//            for (int i = 0; i < chunk.length; i++) {
//                if (chunk[i] == 0) {
//                    if (funnyStr.hasNextNonSpaceChar()) {
//                        throw new RuntimeException("Should not follow after null terminator");
//                    }
//                    for (int j = i + 1; j < chunk.length; j++) {
//                        if (chunk[j] != 0) {
//                            throw new RuntimeException("Should not follow after null terminator");
//                        }
//                    }
//                    break;
//                }
//                else resultLength++;
//            }
//            result.add(chunk);
//        }
//        byte[] resultBytes = new byte[bufferLength];
//        int i = 0;
//        for (byte[] bytes : result) {
//            System.arraycopy(bytes, 0, resultBytes, i, bytes.length);
//            i += bytes.length;
//        }
//        
//        return new String(resultBytes, 0, resultLength);
    }

    private static class PointedString {
        final String string;
        final int totalLen;
        private int index;

        PointedString(String base) {
            this.string = base;
            this.totalLen = string.length();
        }

        boolean hasNextNonSpaceChar() {
            for (int i = index; i < totalLen; i++) {
                if (string.charAt(i) != ' ') return true;
            }
            return false;
        }

        char nextNonSpaceChar() {
            char c;
            while ((c = string.charAt(index++)) == ' ') {
                if (index == totalLen) return '\0';
            }

            return c;
        }
    }
}
