package trashsoftware.duckSonTranslator.translators;

import org.junit.jupiter.api.Test;
import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.result.TranslationResult;

import java.io.*;

public class TranslatorTest {

    public static void main(String[] args) throws Exception {
        TranslatorTest translatorTest = new TranslatorTest();
//        translatorTest.testIntro();
//        translatorTest.testChiChs();
        translatorTest.testChsChi();
//        translatorTest.serializeTest();
    }
    
    @Test
    public void testWorldCup() throws IOException {
        DuckSonTranslator translator = new DuckSonTranslator();

        String positions = "前锋 边锋 中场 后腰 后卫 门将 守门员 裁判 教练";
        TranslationResult result = translator.chsToGeglish(positions);
        System.out.println(result);
    }
    
    @Test
    public void testIntro() throws IOException {
        String s = "Duck Son翻译器是一个具有将中文按每个字翻译为对应的英文单词的搞笑软件。该软件内置了普通话翻译版和重庆话翻译版，具有多种翻译模式，欢迎下载体验。";
        DuckSonTranslator translator = new DuckSonTranslator();
        
        TranslationResult result = translator.chsToChinglish(s);
        System.out.println(result);
    }
    
    @Test
    public void uiTextTest() throws IOException {
        String s = "介质";
        DuckSonTranslator translator = new DuckSonTranslator();

        TranslationResult result = translator.chsToGeglish(s);
        System.out.println(result);
    }
    
    @Test
    public void testChsChi() throws IOException {
        String s = "每个都是傻子";
        DuckSonTranslator translator = new DuckSonTranslator();
        
        TranslationResult result = translator.chsToChinglish(s);
        System.out.println(result);
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
    public void testChiChs() throws IOException {
        String s = "aspirin stop";
        DuckSonTranslator translator = new DuckSonTranslator();

        TranslationResult result = translator.chinglishToChs(s);
        System.out.println(result);
    }
}
