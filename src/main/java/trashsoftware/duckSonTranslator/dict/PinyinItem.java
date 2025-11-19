package trashsoftware.duckSonTranslator.dict;

import java.util.ArrayList;
import java.util.List;

public class PinyinItem {
    
    private final char character;
    private final String nonUtf8;
    private final List<String> pinyinList = new ArrayList<>();  // 数字声调的拼音
    private final List<String> cqPinList = new ArrayList<>();
    private final List<String> pinyinOrigList = new ArrayList<>();  // ā这种形式的拼音
    
    PinyinItem(char character) {
        this.character = character;
        this.nonUtf8 = null;
    }
    
    PinyinItem(String text) {
        this.character = 0;
        this.nonUtf8 = text;
    }
    
    public boolean isChar() {
        return character != 0;
    }
}
