package trashsoftware.duckSonTranslator;

import trashsoftware.duckSonTranslator.wordPickerChsGeg.PickerFactory;

import java.io.IOException;

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
        System.out.println(translator.chsToGeglish("通宵"));
        System.out.println(translator.chsToGeglish("石井坡"));
        System.out.println(translator.geglishToChs("manier arrived where in"));
    }
}
