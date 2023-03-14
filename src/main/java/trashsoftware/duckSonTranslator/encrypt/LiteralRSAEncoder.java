package trashsoftware.duckSonTranslator.encrypt;

import trashsoftware.duckSonTranslator.encrypt.literalConverter.LiteralConverter;
import trashsoftware.duckSonTranslator.encrypt.literalConverter.ThinkCleanConverter;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class LiteralRSAEncoder {
    private final PublicKey publicKey;
    private final LiteralConverter converter;

    public LiteralRSAEncoder(PublicKey publicKey, LiteralConverter converter) {
        this.publicKey = publicKey;
        this.converter = converter;
    }

    public static void main(String[] args) {
        RSAKeyGenerator keyGenerator = RSAKeyGenerator.getInstance();
        keyGenerator.setBits(256, 384);
        KeyPair keyPair = keyGenerator.generate();

        String pub = LiteralRSAUtil.keyToLiteral(keyPair.getPublicKey(), true);
        String pri = LiteralRSAUtil.keyToLiteral(keyPair.getPrivateKey(), true);
        System.out.println("Public key: " + pub);
        System.out.println("Private key: " + pri);

        LiteralRSAEncoder encoder = new LiteralRSAEncoder(keyPair.getPublicKey(),
                ThinkCleanConverter.getInstance());
        String enc = encoder.encode("This is the test encoding string.");

        System.out.println("Encoded string: " + enc);

        LiteralRSADecoder decoder = new LiteralRSADecoder(LiteralRSAUtil.literalToPrivateKey(pri),
                ThinkCleanConverter.getInstance());
        String dec = decoder.decode(enc);
        System.out.println("Decoded string: " + dec);
    }

    public String encode(String text) {
        byte[] byteText = text.getBytes(StandardCharsets.UTF_8);
        EncodeSpec encodeSpec = new EncodeSpec(publicKey);

        int resBigIntLength = byteText.length / encodeSpec.getInChunkByteLength();
        if (byteText.length % encodeSpec.getInChunkByteLength() != 0) resBigIntLength++;
        BigInteger[] result = new BigInteger[resBigIntLength];

        int resultIndex = 0;
        for (int i = 0; i < byteText.length; i += encodeSpec.getInChunkByteLength()) {
            BigInteger digit = LiteralRSAUtil.bytesToBigInt(byteText, i, encodeSpec);
            result[resultIndex++] = RSA.encryptOne(publicKey, digit);
        }
        StringBuilder builder = new StringBuilder();
        for (BigInteger bigInteger : result) {
            builder.append(converter.numToString(bigInteger)).append(converter.getSplitChar());
        }
        builder.setLength(builder.length() - 1);

        return converter.randomPostProcess(builder.toString());
    }
}
