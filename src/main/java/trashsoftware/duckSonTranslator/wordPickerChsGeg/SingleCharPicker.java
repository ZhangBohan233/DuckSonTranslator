package trashsoftware.duckSonTranslator.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;

import java.util.HashMap;
import java.util.Map;

public abstract class SingleCharPicker extends WordPicker {
    
    protected final Map<Character, Result> cache = new HashMap<>();
    
    protected SingleCharPicker(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }

    @Override
    public Result translateWord(String sentence) {
        char c = sentence.charAt(0);
        Result res = cache.computeIfAbsent(c, k -> translateChar(c));  // 优化，避免同一个字重复查表，null也在考虑范围内
        return res == Result.NOT_FOUND ? null : res;
    }

    /**
     * 返回找到的最佳结果。如果没找到，最好返回{@link Result#NOT_FOUND}，可以节省重复查表的时间。
     */
    protected abstract Result translateChar(char chs);
}
