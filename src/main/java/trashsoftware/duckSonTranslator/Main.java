package trashsoftware.duckSonTranslator;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        DuckSonTranslator translator = new DuckSonTranslator(true);
        System.out.println(translator.getCoreVersion() + "." + translator.getDictionaryVersion());
        String geglish = translator.chsToGeglish("什么鸡巴东西正在打的GE1234，真是吃了屎了，的确。");
        System.out.println(geglish);
        String chs = translator.geglishToChs(geglish);
        System.out.println(chs);

        System.out.println(translator.chsToGeglish("喝酒"));
        System.out.println(translator.chsToGeglish("你莫豁我"));
    }
}
