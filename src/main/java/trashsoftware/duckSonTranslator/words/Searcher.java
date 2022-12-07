package trashsoftware.duckSonTranslator.words;

import java.util.List;

public abstract class Searcher {
    
    protected final DuckSonDictionary parent;
    protected final String srcLang;
    protected final String dstLang;
    
    protected Searcher(DuckSonDictionary parent, String srcLang, String dstLang) {
        this.parent = parent;
        this.srcLang = srcLang;
        this.dstLang = dstLang;
    }
    
    public abstract List<WordResult> search(String text);
}
