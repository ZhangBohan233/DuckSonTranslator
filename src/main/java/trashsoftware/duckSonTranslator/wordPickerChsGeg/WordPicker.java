package trashsoftware.duckSonTranslator.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;

public abstract class WordPicker {
    protected final PickerFactory factory;
    protected final BigDict bigDict;
    
    protected WordPicker(BigDict bigDict, PickerFactory factory) {
        this.bigDict = bigDict;
        this.factory = factory;
    }
    
    public abstract MatchResult translateWord(String sentence);

    public PickerFactory getFactory() {
        return factory;
    }
}
