package trashsoftware.duckSonTranslator.wordPickers;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg.ResultFromChs;

public abstract class PickerFromChs extends Picker {
    public PickerFromChs(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }

    @Override
    public abstract ResultFromChs translate(String original);
}
