package trashsoftware.duckSonTranslator.dict;

import trashsoftware.duckSonTranslator.dict.DictMaker;
import trashsoftware.duckSonTranslator.translators.Translator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PinyinDict extends Translator {
    
    protected Map<Character, String> pinyin;
    protected Map<String, List<Character>> pinyinToChs = new HashMap<>();
    
    public PinyinDict() throws IOException {
        pinyin = DictMaker.getChsPinyinDict();
        makeRevPinyinDict();
    }
    
    private void makeRevPinyinDict() {
        for (Map.Entry<Character, String> entry : pinyin.entrySet()) {
            List<Character> sameSoundChar = 
                    pinyinToChs.computeIfAbsent(entry.getValue(), k -> new ArrayList<>());
            sameSoundChar.add(entry.getKey());
        }
    }
    
    public String getPinyinByChs(char ch) {
        return pinyin.get(ch);
    }
    
    public List<Character> getChsListByPinyin(String pinyin) {
        return pinyinToChs.get(pinyin);
    }
}
