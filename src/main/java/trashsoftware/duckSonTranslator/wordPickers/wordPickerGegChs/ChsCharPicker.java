package trashsoftware.duckSonTranslator.wordPickers.wordPickerGegChs;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.wordPickers.ResultFromLatin;
import trashsoftware.duckSonTranslator.wordPickers.Picker;
import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class ChsCharPicker extends Picker {

    public static final Set<Character> CHS_EXCEPTIONS = Set.of(
            '的', '（', '）', '(', ')'
    );
    
    protected final Map<String, ResultFromLatin> cache = new HashMap<>();
    
    public ChsCharPicker(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }
    
    public ResultFromLatin translate(String word) {
        ResultFromLatin res = cache.computeIfAbsent(word, k -> translateOneWordInner(word));
        return res == ResultFromLatin.NOT_FOUND ? null : res;
    }
    
    protected abstract ResultFromLatin translateOneWordInner(String word);
}
