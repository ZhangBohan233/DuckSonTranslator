package trashsoftware.duckSonTranslator.encrypt.literalConverter;

import trashsoftware.duckSonTranslator.encrypt.InvalidLiteralException;

public class HexLiteralConverter extends CharLiteralConverter {
    private static HexLiteralConverter instance;
    
    protected HexLiteralConverter() {
        super('+', 16);
    }

    public static HexLiteralConverter getInstance() {
        if (instance == null) instance = new HexLiteralConverter();
        return instance;
    }

    @Override
    protected char numToChar(int num) {
        if (num < 10) return (char) (num + '0');
        else return (char) (num - 10 + 'A');
    }

    @Override
    protected int charToNum(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        } else if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        } else {
            throw new InvalidLiteralException(String.valueOf(c));
        }
    }
}
