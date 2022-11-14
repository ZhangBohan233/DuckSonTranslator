package trashsoftware.duckSonTranslator.wordPickerGegChs;

import trashsoftware.duckSonTranslator.dict.BigDict;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class ChsCharPicker {

    public static final Set<Character> CHS_EXCEPTIONS = Set.of(
            '的', '（', '）', '(', ')'
    );
    
    protected BigDict bigDict;
    protected ChsPickerFactory factory;
    protected final Map<String, ChsResult> cache = new HashMap<>();
    
    public ChsCharPicker(BigDict bigDict, ChsPickerFactory factory) {
        this.bigDict = bigDict;
        this.factory = factory;
    }
    
    public ChsResult translateOneWord(String word) {
        ChsResult res = cache.computeIfAbsent(word, k -> translateOneWordInner(word));
        return res == ChsResult.NOT_FOUND ? null : res;
    }
    
    protected abstract ChsResult translateOneWordInner(String word);
}
