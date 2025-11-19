package trashsoftware.duckSonTranslator;

import trashsoftware.duckSonTranslator.options.TranslatorOptions;
import trashsoftware.duckSonTranslator.result.TranslationResult;
import trashsoftware.duckSonTranslator.translators.DuckSonTranslator;
import trashsoftware.duckSonTranslator.words.DuckSonDictionary;
import trashsoftware.duckSonTranslator.words.WordResult;
import trashsoftware.duckSonTranslator.words.WordResultType;

import java.io.IOException;
import java.util.List;

public class Main {
    
    private static void worldCupCountries() throws IOException {
        String s = "" +
                "A组：卡塔尔（A1）、厄瓜多尔（A2）、塞内加尔（A3）、荷兰（A4）\n" +
                "\n" +
                "B组：英格兰（B1）、伊朗（B2）、美国（B3）、威尔士（B4）\n" +
                "\n" +
                "C组：阿根廷（C1）、沙特（C2）、墨西哥（C3）、波兰（C4）\n" +
                "\n" +
                "D组：法国（D1）、澳大利亚（D2）、丹麦（D3）、突尼斯（D4）\n" +
                "\n" +
                "E组：西班牙（E1）、哥斯达黎加（E2）、德国（E3）、日本（E4）\n" +
                "\n" +
                "F组：比利时（F1）、加拿大（F2）、摩洛哥（F3）、克罗地亚（F4）\n" +
                "\n" +
                "G组：巴西（G1）、塞尔维亚（G2）、瑞士（G3）、喀麦隆（G4）\n" +
                "\n" +
                "H组：葡萄牙（H1）、加纳（H2）、乌拉圭（H3）、韩国（H4）淘汰";
        DuckSonTranslator translator = new DuckSonTranslator();
        TranslationResult result = translator.chsToGeglish(s);
        System.out.println(result);
    }
    
    private static void testDictionary() throws IOException {
        TranslatorOptions options = TranslatorOptions.getInstance();
//        options.setUseSameSoundChar(false);
        DuckSonDictionary dictionary = new DuckSonDictionary(options);
//        List<WordResult> wordResults = dictionary.search("x光", "chs", "geg");
//        System.out.println("=====");
//        for (WordResult wr : wordResults) {
//            System.out.println(wr);
//        }
        List<WordResult> wordResults3 = dictionary.search("压", "chs", "geg");
        System.out.println("=====");
        for (WordResult wr : wordResults3) {
            System.out.println(wr);
        }

        List<WordResult> wordResults2 = dictionary.search("look", "geg", "chs");
        System.out.println("=====");
        for (WordResult wr : wordResults2) {
            System.out.println(wr);
        }
    }

    public static void main(String[] args) throws IOException {
        testDictionary();
//        if (true) return;
//        worldCupCountries();
        TranslatorOptions options = TranslatorOptions.getInstance();
//        if (true) return;
//        options.setUseBaseDict(false);
//        options.setUseSameSoundChar(false);
//        options.setChsGegPicker(PickerFactory.COMBINED_CHAR);
//        options.setUseSameSoundChar(false);
        
        DuckSonTranslator translator = new DuckSonTranslator(options);
        System.out.println(translator.getCoreVersion() + "." + translator.getDictionaryVersion());
        
//        translator.getOptions().setChongqingMode(false);

//        TranslationResult geglish = translator.chsToGeglish("什么鸡巴东西的人正在打的GE1234，真是吃了屎了，的确。");
//        System.out.println(geglish);
//        geglish.printTokens();

        System.out.println(translator.chsToGeglish("吓人"));
        System.out.println(translator.chsToGeglish("好了"));
        System.out.println(translator.chsToGeglish("冷却"));
//        System.out.println(translator.geglishToChs("seer shit"));
        System.out.println(translator.chsToGeglish("压力"));
//        System.out.println(translator.geglishToChs("baggage claim"));

//        TranslationResult geglish2 = translator.chsToGeglish("萌♣");
//        System.out.println(geglish2);
//        geglish2.printTokens();
//
//        System.out.println(translator.geglishToChs("de he is fight comen"));
    }
}
