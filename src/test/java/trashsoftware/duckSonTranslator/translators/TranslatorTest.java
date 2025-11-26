package trashsoftware.duckSonTranslator.translators;

import org.junit.jupiter.api.Test;
import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.result.TranslationResult;

import java.io.*;

public class TranslatorTest {

    public static void main(String[] args) throws Exception {
        TranslatorTest translatorTest = new TranslatorTest();
        translatorTest.testChsGeg();
//        translatorTest.testIntro();
//        translatorTest.testChiChs();
        translatorTest.testChsChi();
//        translatorTest.serializeTest();
    }
    
    public void testChsGeg() throws IOException {
        DuckSonTranslator translator = new DuckSonTranslator();

        String positions = "宇宙的";
        TranslationResult result = translator.chsToGeglish(positions);
        System.out.println(result);
    }
    
    @Test
    public void testChongqingPlaceNames() {
        DuckSonTranslator translator = new DuckSonTranslator();
        
        String[][] mapper = new String[][]{
                new String[]{"观音桥", "see music bridge"},
                new String[]{"大学城", "big study city"},
                new String[]{"南坪", "south flat"}
        };
        
        for (String[] maps : mapper) {
            TranslationResult geg = translator.chsToGeglish(maps[0]);
            System.out.println(geg);
            assert geg.toString().equals(maps[1]);
        }
    }
    
    @Test
    public void testWorldCup() {
        DuckSonTranslator translator = new DuckSonTranslator();

        String positions = "前锋 边锋 中场 后腰 后卫 门将 守门员 裁判 教练";
        TranslationResult result = translator.chsToGeglish(positions);
        System.out.println(result);
    }
    
    @Test
    public void testIntro() {
        String s = "Duck Son翻译器是一个具有将中文按每个字翻译为对应的英文单词的搞笑软件。该软件内置了普通话翻译版和重庆话翻译版，具有多种翻译模式，欢迎下载体验。";
        DuckSonTranslator translator = new DuckSonTranslator();
        
        TranslationResult result = translator.chsToChinglish(s);
        System.out.println(result);
    }
    
    @Test
    public void uiTextTest() {
        String s = "介质";
        DuckSonTranslator translator = new DuckSonTranslator();

        TranslationResult result = translator.chsToGeglish(s);
        System.out.println(result);
    }
    
    @Test
    public void testChsChi() {
        String s = "每个都是傻子";
        DuckSonTranslator translator = new DuckSonTranslator();
        
        TranslationResult result = translator.chsToChinglish(s);
        System.out.println(result);
        System.out.println(translator.chsToChinglish("哥斯达黎加"));
    }
    
    @Test
    public void serializeTest() throws IOException, ClassNotFoundException {
        long t0 = System.currentTimeMillis();
        DuckSonTranslator translator = new DuckSonTranslator();
        long t1 = System.currentTimeMillis();
        System.out.println("Create time: " + (t1 - t0));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(translator.bigDict);
        oos.flush();
        oos.close();
        
        byte[] array = baos.toByteArray();
        System.out.println(array.length);
        
        long t2 = System.currentTimeMillis();
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        ObjectInputStream ois = new ObjectInputStream(bais);
        BigDict bd = (BigDict) ois.readObject();
        System.out.println(bd.getVersionStr());
        long t3 = System.currentTimeMillis();

        System.out.println("Load time: " + (t3 - t2));
    }
    
    @Test
    public void testChiChs() {
        String s = "pll slow aspirin stop";
        DuckSonTranslator translator = new DuckSonTranslator();

        TranslationResult result = translator.chinglishToChs(s);
//        System.out.println(result.getResultTokens());
//        System.out.println(result.getResultTokens().get(result.getResultTokens().size() - 1));
        System.out.println(result);
        
        TranslationResult result2 = translator.chsToGeglish("啊啊asdhasihd哈哈哈");
        System.out.println(result2);
//        System.out.println(result2.getResultTokens());
//        assert s.equals(result.toString());
    }
}
