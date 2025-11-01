package trashsoftware.duckSonTranslator.options;

import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;

public class TranslatorOptions {
    
    private static TranslatorOptions instance;
    private boolean chongqingMode = true;
    private boolean useBaseDict = true;
    private boolean useSameSoundChar = true;
    private boolean useHugeDict = false;
    private PickerFactory pickerFactory = PickerFactory.COMBINED_CHAR;
    
    private TranslatorOptions() {
    }

    public static TranslatorOptions getInstance() {
        if (instance == null) {
            instance = new TranslatorOptions();
        }
        return instance;
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

    public boolean isUseHugeDict() {
        return useHugeDict;
    }

    public boolean isUseSameSoundChar() {
        return useSameSoundChar;
    }

    public void setUseSameSoundChar(boolean useSameSoundChar) {
        this.useSameSoundChar = useSameSoundChar;
    }

    public PickerFactory getPicker() {
        return pickerFactory;
    }

    public void setPickerFactory(PickerFactory pickerFactory) {
        this.pickerFactory = pickerFactory;
    }
}
