package trashsoftware.duckSonTranslator;

import trashsoftware.duckSonTranslator.dict.Util;
import trashsoftware.duckSonTranslator.result.ResultToken;
import trashsoftware.duckSonTranslator.result.TranslationResult;
import trashsoftware.duckSonTranslator.wordPickerChsGeg.PickerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        TranslatorOptions options = new TranslatorOptions();
//        options.setUseBaseDict(false);
//        options.setUseSameSoundChar(false);
//        options.setChsGegPicker(PickerFactory.COMBINED_CHAR);
//        options.setUseSameSoundChar(false);
        
        DuckSonTranslator translator = new DuckSonTranslator(options);
        System.out.println(translator.getCoreVersion() + "." + translator.getDictionaryVersion());

        TranslationResult geglish = translator.chsToGeglish("什么鸡巴东西的人正在打的GE1234，真是吃了屎了，的确。");
        System.out.println(geglish);
        geglish.printTokens();

        TranslationResult geglish2 = translator.chsToGeglish("♣是啥，睡着了");
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
