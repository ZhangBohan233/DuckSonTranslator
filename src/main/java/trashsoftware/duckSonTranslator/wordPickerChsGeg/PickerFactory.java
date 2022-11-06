package trashsoftware.duckSonTranslator.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;

import java.lang.reflect.InvocationTargetException;

public enum PickerFactory {
    SINGLE_CHAR_INVERSE_FREQ(InverseFreqCharPicker.class),
    RANDOM_CHAR(RandomCharPicker.class),
    COMMON_PREFIX_CHAR(CommonPrefixCharPicker.class);
    
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
