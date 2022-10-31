package trashsoftware.duckSonTranslator;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        DuckSonTranslator translator = new DuckSonTranslator(true);
        String geglish = translator.chsToGeglish("什么鸡巴东西，真是吃了屎了。");
        System.out.println(geglish);
    }
}
