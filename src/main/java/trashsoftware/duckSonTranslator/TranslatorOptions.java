package trashsoftware.duckSonTranslator;

import trashsoftware.duckSonTranslator.wordPickerChsGeg.PickerFactory;

public class TranslatorOptions {
    
    private boolean chongqingMode = true;
    private boolean useBaseDict = true;
    private boolean useSameSoundChar = true;
    private PickerFactory chsGegPicker = PickerFactory.COMBINED_CHAR;
    
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
}
