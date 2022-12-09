package trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;
import trashsoftware.duckSonTranslator.wordPickers.PickerFromChs;

public abstract class GegWordPicker extends PickerFromChs {

    public GegWordPicker(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }
}
