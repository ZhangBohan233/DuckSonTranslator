package trashsoftware.duckSonTranslator.encrypt.literalConverter;

import trashsoftware.duckSonTranslator.encrypt.InvalidLiteralException;

public class EngLitConverter extends CharLiteralConverter {

    static final char[] ZERO_POOL = {'*', '#', '%', '$'};
    private int meanWordLength = 7;

    protected EngLitConverter() {
        super('/', 52);
    }

    public static EngLitConverter getInstance() {
        return new EngLitConverter();
    }

    public void setMeanWordLength(int meanWordLength) {
        this.meanWordLength = meanWordLength;
    }

    @Override
    public String randomPostProcess(String orig) {
        double chance = 1.0 / meanWordLength;
        boolean lastIsSpace = false;
        StringBuilder result = new StringBuilder();
        for (char c : orig.toCharArray()) {
            result.append(c);
            if (lastIsSpace) {
                lastIsSpace = false;
            } else {
                double rnd = Math.random();
                if (rnd < chance) {
                    result.append(' ');
                    lastIsSpace = true;
                }
            }
        }
        return result.toString();
    }

    @Override
    protected char numToChar(int num) {
        if (num < 26) return (char) (num + 'a');
        if (num < 52) return (char) (num - 26 + 'A');
        throw new RuntimeException("Cannot encode");
    }

    @Override
    protected int charToNum(char c) {
        if (c <= 'z') {
            if (c >= 'a') return c - 'a';
            if (c <= 'Z') {
                if (c >= 'A') return c - 'A' + 26;
            }
        }
        throw new InvalidLiteralException(String.valueOf(c));
    }
}
