package trashsoftware.duckSonTranslator;

import trashsoftware.duckSonTranslator.wordPickerChsGeg.PickerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        TranslatorOptions options = new TranslatorOptions();
//        options.setUseBaseDict(false);
//        options.setUseSameSoundChar(false);
//        options.setChsGegPicker(PickerFactory.COMBINED_CHAR);
//        options.setUseSameSoundChar(false);
        
        DuckSonTranslator translator = new DuckSonTranslator(options);
        System.out.println(translator.getCoreVersion() + "." + translator.getDictionaryVersion());

        String geglish = translator.chsToGeglish("什么鸡巴东西正在打的GE1234，真是吃了屎了，的确。");
        System.out.println(geglish);
        String chs = translator.geglishToChs(geglish);
        System.out.println(chs);

        System.out.println(translator.chsToGeglish("这个b理由已经用了两次了"));
        System.out.println(translator.chsToGeglish("对比敏感度 视觉 感觉"));
        System.out.println(translator.chsToGeglish("刮痧"));
        System.out.println(translator.geglishToChs("no shell's manier arrived where in, shave, shave"));
        var aa = "啊\n" +
                "哦\n" +
                "额\n" +
                "一\n" +
                "我\n" +
                "与\n" +
                "博\n" +
                "破\n" +
                "莫\n" +
                "佛\n" +
                "的\n" +
                "特\n" +
                "呢\n" +
                "了";
        System.out.println(translator.chsToGeglish(aa));
    }
}
