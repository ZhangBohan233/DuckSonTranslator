package trashsoftware.duckSonTranslator.encrypt.literalConverter;

import trashsoftware.duckSonTranslator.encrypt.InvalidLiteralException;

public class ChineseLitConverter extends CharLiteralConverter {
    public static final char MIN = 19968;
    public static final char MAX = 40869;  // inclusive
    public static final int RANGE = MAX - MIN + 1;
    
    private static CharLiteralConverter instance;

    protected ChineseLitConverter() {
        super('/', RANGE + 62);
    }

    public static CharLiteralConverter getInstance() {
        if (instance == null) {
            instance = new ChineseLitConverter();
        }
        return instance;
    }

    @Override
    protected char numToChar(int num) {
        if (num < 10) return (char) (num + '0');
        if (num < 36) return (char) (num - 10 + 'a');
        if (num < 62) return (char) (num - 36 + 'A');
        else if (num < 62 + RANGE) return (char) (num - 62 + MIN);
        throw new RuntimeException("Cannot encode");
    }

    @Override
    protected int charToNum(char c) {
        if (c <= '9') return c - '0';
        else if (c >= 'A' && c <= 'Z') return c - 'A' + 36;
        else if (c >= 'a' && c <= 'z') return c - 'a' + 10;
        else if (c >= MIN && c <= MAX) return c - MIN + 62;
        throw new InvalidLiteralException(String.valueOf(c));
    }
}
