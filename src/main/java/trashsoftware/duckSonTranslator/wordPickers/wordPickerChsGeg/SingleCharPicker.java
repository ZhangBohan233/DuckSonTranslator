package trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class SingleCharPicker extends GegWordPicker {
    
    protected final Map<Character, ResultFromChs> cache = new HashMap<>();
    
    public SingleCharPicker(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }

    @Override
    public ResultFromChs translate(String sentence) {
        char c = sentence.charAt(0);
        ResultFromChs res = cache.computeIfAbsent(c, k -> translateChar(c));  // 优化，避免同一个字重复查表，null也在考虑范围内
        return res == ResultFromChs.NOT_FOUND ? null : res;
    }

    /**
     * 返回找到的最佳结果。如果没找到，最好返回{@link ResultFromChs#NOT_FOUND}，可以节省重复查表的时间。
     */
    protected abstract ResultFromChs translateChar(char chs);
}
