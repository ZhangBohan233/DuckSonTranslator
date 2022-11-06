package trashsoftware.duckSonTranslator.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;

public abstract class SingleCharPicker extends WordPicker {
    
    protected SingleCharPicker(BigDict bigDict) {
        super(bigDict);
    }

    @Override
    public Result translateWord(String sentence) {
        return translateChar(sentence.charAt(0));
    }
    
    protected abstract Result translateChar(char chs);
}
