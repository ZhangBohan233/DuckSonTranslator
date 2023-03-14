package trashsoftware.duckSonTranslator.encrypt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class UtilFunctionsTest {
    
    @Test
    public void funnyTest() {
        for (int i = 0; i < 64; i++) {
            char c = LiteralRSAUtil.intToFunny(i);
            Assertions.assertEquals(i, LiteralRSAUtil.funnyToInt(c));
        }
    }

    @Test
    public void sixtyOneTest() {
        for (int i = 0; i < 61; i++) {
            char c = LiteralRSAUtil.numToChar61(i);
            Assertions.assertEquals(i, LiteralRSAUtil.char61ToNum(c));
        }
    }
}
