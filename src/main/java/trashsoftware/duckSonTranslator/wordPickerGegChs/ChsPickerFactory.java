package trashsoftware.duckSonTranslator.wordPickerGegChs;

import trashsoftware.duckSonTranslator.dict.BigDict;

public enum ChsPickerFactory {
    NAIVE_PICKER(NaiveChsPicker.class);

    private final Class<? extends ChsCharPicker> pickerClass;

    ChsPickerFactory(Class<? extends ChsCharPicker> pickerClass) {
        this.pickerClass = pickerClass;
    }

    public ChsCharPicker create(BigDict bigDict) {
        try {
            return pickerClass
                    .getDeclaredConstructor(BigDict.class, this.getClass())
                    .newInstance(bigDict, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
