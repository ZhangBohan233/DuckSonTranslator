package trashsoftware.duckSonTranslator.wordPickers.chitochs;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.wordPickers.Picker;
import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;
import trashsoftware.duckSonTranslator.wordPickers.ResultFromLatin;

public abstract class ChiChsWordPicker extends Picker {
    public ChiChsWordPicker(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }

    @Override
    public abstract ResultFromLatin translate(String original);
}
