package trashsoftware.duckSonTranslator;

import trashsoftware.duckSonTranslator.result.TranslationResult;
import trashsoftware.duckSonTranslator.translators.DuckSonTranslator;

import java.io.IOException;

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

    public static void main(String[] args) throws IOException {
        worldCupCountries();
        TranslatorOptions options = new TranslatorOptions();
//        options.setUseBaseDict(false);
//        options.setUseSameSoundChar(false);
//        options.setChsGegPicker(PickerFactory.COMBINED_CHAR);
//        options.setUseSameSoundChar(false);
        
        DuckSonTranslator translator = new DuckSonTranslator(options);
        System.out.println(translator.getCoreVersion() + "." + translator.getDictionaryVersion());

//        TranslationResult geglish = translator.chsToGeglish("什么鸡巴东西的人正在打的GE1234，真是吃了屎了，的确。");
//        System.out.println(geglish);
//        geglish.printTokens();

        System.out.println(translator.chsToGeglish("位置"));

        TranslationResult geglish2 = translator.chsToGeglish("我们你们她们♣是啥，睡着了，日薪越亿");
        System.out.println(geglish2);
        geglish2.printTokens();

        System.out.println(translator.geglishToChs("de he is fight comen"));
//        System.out.println(geglish.findTokensInRange(3, 5));
//        String chs = translator.geglishToChs(geglish);
//        System.out.println(chs);
//
//        System.out.println(translator.chsToGeglish("这个b理由已经用了两次了"));
//        System.out.println(translator.chsToGeglish("对比敏感度 视觉 感觉"));
//        System.out.println(translator.chsToGeglish("是打来的"));
//        var chs = translator.geglishToChs(
//                "no shell's manier arrived where in, \n" +
//                "we shaving and iron shave.\n" + 
//                "then taking taxi to where. is confirm fuck");
//        System.out.println(chs);
//        chs.printTokens();  // todo
//        List<ResultToken> engRange = chs.findTokensInRange(3, 4);
//        System.out.println(engRange);
//        var chsRange = TranslationResult.rangeOf(engRange);
//        System.out.println(Util.listOfArrayToString(chsRange));

//        List<ResultToken> rts = new ArrayList<>();
//        rts.add(new ResultToken("a", 0, 2));
//        rts.add(new ResultToken("b", 2, 1));
//        rts.add(new ResultToken("b", 3, 3));
//
//        rts.add(new ResultToken("b", 7, 3));
//        rts.add(new ResultToken("b", 10, 1));
//        
//        var intRes = TranslationResult.rangeOf(rts);
//        for (int[] aaa : intRes) System.out.println(Arrays.toString(aaa));
//        var aa = "啊\n" +
//                "哦\n" +
//                "额\n" +
//                "一\n" +
//                "我\n" +
//                "与\n" +
//                "博\n" +
//                "破\n" +
//                "莫\n" +
//                "佛\n" +
//                "的\n" +
//                "特\n" +
//                "呢\n" +
//                "了";
//        System.out.println(translator.chsToGeglish(aa));
    }
}
