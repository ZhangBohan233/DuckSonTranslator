package trashsoftware.duckSonTranslator.translators;

import trashsoftware.duckSonTranslator.TranslatorOptions;
import trashsoftware.duckSonTranslator.dict.BaseDict;
import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.PinyinDict;
import trashsoftware.duckSonTranslator.grammar.GrammarDict;
import trashsoftware.duckSonTranslator.result.TranslationResult;
import trashsoftware.duckSonTranslator.wordPickerChsGeg.PickerFactory;
import trashsoftware.duckSonTranslator.wordPickerChsGeg.WordPicker;
import trashsoftware.duckSonTranslator.wordPickerGegChs.ChsCharPicker;
import trashsoftware.duckSonTranslator.wordPickerGegChs.ChsPickerFactory;

import java.io.IOException;

public class DuckSonTranslator {
    public static final String CORE_VERSION = "0.6.0";

    final BaseDict baseDict;
    final PinyinDict pinyinDict;
    final BigDict bigDict;
    final GrammarDict grammarDict;
    final TranslatorOptions options;
    WordPicker chsToGegPicker;
    ChsCharPicker gegToChsPicker;

    public DuckSonTranslator(TranslatorOptions options) throws IOException {
        this.options = options;
        this.baseDict = new BaseDict();
        this.pinyinDict = new PinyinDict();
        this.bigDict = new BigDict();
        this.grammarDict = new GrammarDict();

        createChsGegPicker();
        createGegChsPicker();
    }

    public DuckSonTranslator() throws IOException {
        this(new TranslatorOptions());
    }

    public String getCoreVersion() {
        return CORE_VERSION;
    }

    public String getDictionaryVersion() {
        return baseDict.getVersionStr() + "." + pinyinDict.getVersionStr() + "." + bigDict.getVersionStr();
    }

    private void createChsGegPicker() {
        this.chsToGegPicker = options.getChsGegPicker().create(bigDict);
    }

    private void createGegChsPicker() {
        this.gegToChsPicker = options.getGegChsPicker().create(bigDict);
    }

    @SuppressWarnings("unused")
    public String autoDetectLanguage(String input) {
        int totalLen = input.length();
        int chsCount = 0;
        int engCount = 0;
        int othersCount = 0;

        for (char c : input.toCharArray()) {
            if (c >= 'A' && c <= 'z') engCount++;
            else if (Translator.ENG_PUNCTUATIONS.containsKey(c)) engCount++;
            else if (pinyinDict.getPinyinByChs(c) != null) chsCount++;
            else if (Translator.CHS_PUNCTUATIONS.containsKey(c)) chsCount++;
            else othersCount++;
        }

        if ((double) chsCount / totalLen > 0.75) return "chs";
        if ((double) engCount / totalLen > 0.75) return "geg";

        int subTotal = chsCount + engCount;
        if ((double) chsCount / subTotal > 0.8) return "chs";
        if ((double) engCount / subTotal > 0.8) return "geg";

        return "unk";
    }

    @SuppressWarnings("unused")
    public boolean isUseBaseDict() {
        return options.isUseBaseDict();
    }

    @SuppressWarnings("unused")
    public void setUseBaseDict(boolean useSameSoundChar) {
        options.setUseBaseDict(useSameSoundChar);
    }

    @SuppressWarnings("unused")
    public boolean isChongqingMode() {
        return options.isChongqingMode();
    }

    @SuppressWarnings("unused")
    public void setChongqingMode(boolean chongqingMode) {
        options.setChongqingMode(chongqingMode);
    }

    @SuppressWarnings("unused")
    public boolean isUseSameSoundChar() {
        return options.isUseSameSoundChar();
    }

    @SuppressWarnings("unused")
    public void setUseSameSoundChar(boolean useSameSoundChar) {
        options.setUseSameSoundChar(useSameSoundChar);
    }

    @SuppressWarnings("unused")
    public WordPicker getChsGegPicker() {
        return this.chsToGegPicker;
    }

    @SuppressWarnings("unused")
    public void setChsGegPicker(PickerFactory chsGegPicker) {
        options.setChsGegPicker(chsGegPicker);
        createChsGegPicker();
    }

    @SuppressWarnings("unused")
    public ChsCharPicker getGegToChsPicker() {
        return gegToChsPicker;
    }

    @SuppressWarnings("unused")
    public void setGegChsPicker(ChsPickerFactory gegChsPicker) {
        options.setGegChsPicker(gegChsPicker);
        createGegChsPicker();
    }

    public TranslationResult chsToGeglish(String chs) {
        return ChsToGegTranslator.getInstance(this).translate(chs);
    }

    public TranslationResult geglishToChs(String geglish) {
        return GegToChsTranslator.getInstance(this).translate(geglish);
    }
}
