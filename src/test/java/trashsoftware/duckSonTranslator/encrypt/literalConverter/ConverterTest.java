package trashsoftware.duckSonTranslator.encrypt.literalConverter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class ConverterTest {
    @Test
    public void chineseLitTest() {
        CharLiteralConverter converter = new ChineseLitConverter();
        for (int i = 0; i < converter.getCarry(); i++) {
            char c = converter.numToChar(i);
            Assertions.assertEquals(i, converter.charToNum(c));
        }
    }

    @Test
    public void hexLitTest() {
        CharLiteralConverter converter = new HexLiteralConverter();
        for (int i = 0; i < converter.getCarry(); i++) {
            char c = converter.numToChar(i);
            Assertions.assertEquals(i, converter.charToNum(c));
        }
    }
    
    @Test
    public void thinkCleanTest() {
        MappedLiteralConverter converter = new ThinkCleanConverter();
        BigInteger bigInteger = new BigInteger("139823792379723");
        String clean = converter.numToString(bigInteger);
        BigInteger rev = converter.stringToNum(clean);
        System.out.println(rev);
    }
}
