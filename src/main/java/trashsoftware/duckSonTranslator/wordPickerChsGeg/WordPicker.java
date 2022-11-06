package trashsoftware.duckSonTranslator.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;

public abstract class WordPicker {
    protected final BigDict bigDict;
    
    protected WordPicker(BigDict bigDict) {
        this.bigDict = bigDict;
    }
    
    public abstract Result translateWord(String sentence);
}
