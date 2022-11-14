package trashsoftware.duckSonTranslator;

import trashsoftware.duckSonTranslator.wordPickerChsGeg.PickerFactory;
import trashsoftware.duckSonTranslator.wordPickerGegChs.ChsPickerFactory;

public class TranslatorOptions {
    
    private boolean chongqingMode = true;
    private boolean useBaseDict = true;
    private boolean useSameSoundChar = true;
    private PickerFactory chsGegPicker = PickerFactory.COMBINED_CHAR;
    private ChsPickerFactory gegChsPicker = ChsPickerFactory.NAIVE_PICKER;
    
    public TranslatorOptions() {
    }

    public boolean isChongqingMode() {
        return chongqingMode;
    }

    public void setChongqingMode(boolean chongqingMode) {
        this.chongqingMode = chongqingMode;
    }

    public boolean isUseBaseDict() {
        return useBaseDict;
    }

    public void setUseBaseDict(boolean useBaseDict) {
        this.useBaseDict = useBaseDict;
    }

    public boolean isUseSameSoundChar() {
        return useSameSoundChar;
    }

    public void setUseSameSoundChar(boolean useSameSoundChar) {
        this.useSameSoundChar = useSameSoundChar;
    }

    public PickerFactory getChsGegPicker() {
        return chsGegPicker;
    }

    public void setChsGegPicker(PickerFactory chsGegPicker) {
        this.chsGegPicker = chsGegPicker;
    }

    public ChsPickerFactory getGegChsPicker() {
        return gegChsPicker;
    }

    public void setGegChsPicker(ChsPickerFactory gegChsPicker) {
        this.gegChsPicker = gegChsPicker;
    }
}
