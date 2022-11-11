package trashsoftware.duckSonTranslator.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;

public enum PickerFactory {
    INVERSE_FREQ_CHAR(InverseFreqCharPicker.class),
    RANDOM_CHAR(RandomCharPicker.class),
    COMMON_PREFIX_CHAR(CommonPrefixCharPicker.class),
    COMBINED_CHAR(CombinedCharPicker.class);

    private final Class<? extends WordPicker> pickerClass;

    PickerFactory(Class<? extends WordPicker> pickerClass) {
        this.pickerClass = pickerClass;
    }

    public WordPicker create(BigDict bigDict) {
        try {
            return pickerClass
                    .getDeclaredConstructor(BigDict.class, this.getClass())
                    .newInstance(bigDict, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
