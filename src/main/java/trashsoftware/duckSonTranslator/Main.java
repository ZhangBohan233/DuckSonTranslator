package trashsoftware.duckSonTranslator;

import trashsoftware.duckSonTranslator.dict.BigDict;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
//        PinyinTranslator pinyin = new PinyinTranslator();
//        BigDict bigDict = new BigDict();
//        SuffixTree<Integer> st = new SuffixTree<>();
//        st.insert("mississippi", 1);
        
        DuckSonTranslator translator = new DuckSonTranslator();
        String geglish = translator.chsToGeglish("溜了迈");
        System.out.println(geglish);
//        translator.chsToGeglish("经什么");
    }
}
