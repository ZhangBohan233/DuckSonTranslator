package trashsoftware.duckSonTranslator.encrypt.literalConverter;

import java.math.BigInteger;

public abstract class LiteralConverter {

    protected final char splitChar;
    protected final int carry;
    protected final BigInteger bigIntCarry;

    protected LiteralConverter(char splitChar, int carry) {
        this.splitChar = splitChar;
        this.carry = carry;
        this.bigIntCarry = BigInteger.valueOf(carry);
    }
    
    public String randomPostProcess(String orig) {
        return orig;
    }

    public abstract String numToString(BigInteger num);

    public abstract BigInteger stringToNum(String string);

    public char getSplitChar() {
        return splitChar;
    }

    public int getCarry() {
        return carry;
    }
}
