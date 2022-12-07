package trashsoftware.duckSonTranslator.words;

import java.io.IOException;
import java.util.List;

public class WordsTest {

    public static void main(String[] args) throws IOException {
        testSimple();
    }
    
    public static void testSimple() throws IOException {
        DuckSonDictionary duckSonDictionary = new DuckSonDictionary();
        List<WordResult> wordResults = duckSonDictionary.search("很好", "chs", "geg");

//        System.out.println(wordResults);
        
        List<WordResult> resByEng1 = duckSonDictionary.search("is", "geg", "chs");
        System.out.println(resByEng1);
        List<WordResult> resByEng2 = duckSonDictionary.search("yes", "geg", "chs");
        System.out.println(resByEng2);
    }
}
