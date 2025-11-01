package trashsoftware.duckSonTranslator.words;

import java.util.Collections;
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
    
    public List<WordResult> search(String text) {
        List<WordResult> results = searchByText(text);
        Collections.sort(results);
        return results;
    }

    protected abstract List<WordResult> searchByText(String text);
}
