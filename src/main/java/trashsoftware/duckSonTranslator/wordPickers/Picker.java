package trashsoftware.duckSonTranslator.wordPickers;

import trashsoftware.duckSonTranslator.dict.BigDict;

public abstract class Picker {
    
    protected final BigDict bigDict;
    protected final PickerFactory factory;
    
    public Picker(BigDict bigDict, PickerFactory factory) {
        this.bigDict = bigDict;
        this.factory = factory;
    }
    
    public abstract Result translate(String original);

    public BigDict getBigDict() {
        return bigDict;
    }

    public PickerFactory getFactory() {
        return factory;
    }
}
