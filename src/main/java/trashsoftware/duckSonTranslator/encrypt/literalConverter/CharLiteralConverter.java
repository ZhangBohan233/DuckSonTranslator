package trashsoftware.duckSonTranslator.encrypt.literalConverter;

import java.math.BigInteger;

public abstract class CharLiteralConverter extends LiteralConverter {
    
    protected CharLiteralConverter(char splitChar, int carry) {
        super(splitChar, carry);
    }

    protected abstract char numToChar(int num);

    protected abstract int charToNum(char c);

    public String numToString(BigInteger num) {
        StringBuilder builder = new StringBuilder();

        while (num.compareTo(BigInteger.ZERO) != 0) {
            BigInteger[] divRem = num.divideAndRemainder(bigIntCarry);
            builder.append(numToChar(divRem[1].intValue()));
            num = divRem[0];
        }

        builder.reverse();
        return builder.toString();
    }

    public BigInteger stringToNum(String string) {
        BigInteger result = BigInteger.ZERO;
        for (char c : string.toCharArray()) {
            result = result.multiply(bigIntCarry);
            result = result.add(BigInteger.valueOf(charToNum(c)));
        }

        return result;
    }
}
