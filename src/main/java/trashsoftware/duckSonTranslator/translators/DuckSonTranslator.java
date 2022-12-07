package trashsoftware.duckSonTranslator.translators;

import trashsoftware.duckSonTranslator.TranslatorOptions;
import trashsoftware.duckSonTranslator.dict.BaseDict;
import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.PinyinDict;
import trashsoftware.duckSonTranslator.grammar.GrammarDict;
import trashsoftware.duckSonTranslator.result.TranslationResult;
import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;
import trashsoftware.duckSonTranslator.wordPickers.chitochs.ChiChsWordPicker;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsChi.ChsChiWordPicker;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg.GegWordPicker;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerGegChs.ChsCharPicker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class DuckSonTranslator {
    public static final String CORE_VERSION = "0.7.4";

    final BaseDict baseDict;
    final PinyinDict pinyinDict;
    final BigDict bigDict;
    final GrammarDict grammarDict;
    transient final TranslatorOptions options;
    transient final Map<String, Map<String, Class<? extends Translator>>> langIdAndTranslators = Map.of(
            "chs", Map.of(
                    "chs", IdentityTranslator.class,
                    "geg", ChsToGegTranslator.class,
                    "chi", ChsToChiTranslator.class
            ),
            "geg", Map.of(
                    "chs", GegToChsTranslator.class,
                    "geg", IdentityTranslator.class,
                    "chi", GegToChiTranslator.class
            ),
            "chi", Map.of(
                    "chs", ChiToChsTranslator.class,
                    "geg", ChiToGegTranslator.class,
                    "chi", IdentityTranslator.class
            )
    );
    transient GegWordPicker chsToGegPicker;
    transient ChsCharPicker gegToChsPicker;
    transient ChsChiWordPicker chsToChiPicker;
    transient ChiChsWordPicker chiToChsPicker;

    public DuckSonTranslator(TranslatorOptions options) throws IOException {
        this.options = options;
        this.baseDict = BaseDict.getInstance();
        this.pinyinDict = PinyinDict.getInstance();
        this.bigDict = BigDict.getInstance();
        this.grammarDict = new GrammarDict();

        createPickerInstances();
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

    private void createPickerInstances() {
        this.chsToGegPicker = options.getPicker().createChsToGeg(bigDict);
        this.gegToChsPicker = options.getPicker().createGegToChs(bigDict);
        this.chsToChiPicker = options.getPicker().createChsToChi(bigDict);
        this.chiToChsPicker = options.getPicker().createChiToChs(bigDict);
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
    public GegWordPicker getChsGegPicker() {
        return this.chsToGegPicker;
    }

    @SuppressWarnings("unused")
    public void setPickers(PickerFactory pickerFactory) {
        options.setPickerFactory(pickerFactory);
        createPickerInstances();
    }

    @SuppressWarnings("unused")
    public ChsCharPicker getGegToChsPicker() {
        return gegToChsPicker;
    }

    public TranslationResult chsToGeglish(String chs) {
        return new ChsToGegTranslator(this).translate(chs);
    }

    public TranslationResult geglishToChs(String geglish) {
        return new GegToChsTranslator(this).translate(geglish);
    }

    public TranslationResult chsToChinglish(String chs) {
        return new ChsToChiTranslator(this).translate(chs);
    }

    public TranslationResult chinglishToChs(String chinglish) {
        return translateByLangCode(chinglish, "chi", "chs");
    }

    public TranslationResult geglishToChinglish(String geglish) {
        return translateByLangCode(geglish, "geg", "chi");
    }

    public TranslationResult chinglishToGeglish(String chinglish) {
        return translateByLangCode(chinglish, "chi", "geg");
    }

    /**
     * 按照给定的srcLang和dstLang code来翻译
     * srcLang和dstLang都为{chs, geg, chi}中的某个，否则返回null
     *
     * @param text    要翻译的文本
     * @param srcLang 源语言
     * @param dstLang 目标语言
     * @return 翻译结果
     */
    public TranslationResult translateByLangCode(String text, String srcLang, String dstLang) {
        Map<String, Class<? extends Translator>> srcMap = langIdAndTranslators.get(srcLang);
        if (srcMap != null) {
            Class<? extends Translator> klass = srcMap.get(dstLang);
            if (klass != null) {
                try {
                    Translator translator = klass.getDeclaredConstructor(DuckSonTranslator.class)
                            .newInstance(this);
                    return translator.translate(text);
                } catch (NoSuchMethodException |
                        InvocationTargetException |
                        InstantiationException |
                        IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
