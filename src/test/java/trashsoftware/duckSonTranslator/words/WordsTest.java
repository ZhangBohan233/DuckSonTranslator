package trashsoftware.duckSonTranslator.words;

import trashsoftware.duckSonTranslator.TranslatorOptions;

import java.io.IOException;
import java.util.List;

public class WordsTest {

    public static void main(String[] args) throws IOException {
        testSimple();
    }
    
    public static void testSimple() throws IOException {
        DuckSonDictionary duckSonDictionary = new DuckSonDictionary(TranslatorOptions.getInstance());
        List<WordResult> wordResults = duckSonDictionary.search("高", "chs", "geg");

        System.out.println(wordResults);

        System.out.println(duckSonDictionary.search("expr", "geg", "chs"));
//        List<WordResult> resByEng1 = duckSonDictionary.search("is", "geg", "chs");
//        System.out.println(resByEng1);
//        List<WordResult> resByEng2 = duckSonDictionary.search("yes", "geg", "chs");
//        System.out.println(resByEng2);
    }
}
