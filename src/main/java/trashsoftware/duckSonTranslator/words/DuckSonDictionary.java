package trashsoftware.duckSonTranslator.words;

import trashsoftware.duckSonTranslator.dict.BaseDict;
import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.PinyinDict;

import java.io.IOException;
import java.util.List;

public class DuckSonDictionary {

    final BaseDict baseDict;
    final BigDict bigDict;
    final PinyinDict pinyinDict;

    public DuckSonDictionary() throws IOException {
        this.baseDict = BaseDict.getInstance();
        this.bigDict = BigDict.getInstance();
        this.pinyinDict = PinyinDict.getInstance();
    }
    
    public String inferSrcLang(String text) {
        int chsCount = 0;
        int engCount = 0;
        
        for (char c : text.toCharArray()) {
            if (pinyinDict.getPinyinByChs(c) != null) {
                chsCount++;
            } else if (c >= 'A' && c <= 'z') {
                engCount++;
            }
        }
        
        if (chsCount > 0) return "chs";
        else if (engCount > 0) return "geg";
        else return "unk";
    }

    public List<WordResult> search(String text, String srcLang, String dstLang) {
        if ("chs".equals(srcLang)) {
            if ("geg".equals(dstLang)) {
                return new ChsGegSearcher(this).search(text);
            }
        } else if ("geg".equals(srcLang)) {
            if ("chs".equals(dstLang)) {
                return new GegChsSearcher(this).search(text);
            }
        }
        return null;
    }
}
