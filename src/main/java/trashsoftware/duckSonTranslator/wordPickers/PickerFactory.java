package trashsoftware.duckSonTranslator.wordPickers;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.wordPickers.chitochs.ChiChsWordPicker;
import trashsoftware.duckSonTranslator.wordPickers.chitochs.NaiveChiChsWordPicker;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsChi.ChsChiWordPicker;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsChi.CombinedChsChiPicker;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg.*;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerGegChs.ChsCharPicker;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerGegChs.NaiveChsPicker;

public enum PickerFactory {
    INVERSE_FREQ_CHAR(
            InverseFreqCharPicker.class,
            NaiveChsPicker.class,
            CombinedChsChiPicker.class,
            NaiveChiChsWordPicker.class),
    RANDOM_CHAR(
            RandomCharPicker.class,
            NaiveChsPicker.class,
            CombinedChsChiPicker.class,
            NaiveChiChsWordPicker.class),
    COMMON_PREFIX_CHAR(
            CommonPrefixCharPicker.class,
            NaiveChsPicker.class,
            CombinedChsChiPicker.class,
            NaiveChiChsWordPicker.class),
    COMBINED_CHAR(
            CombinedCharPicker.class,
            NaiveChsPicker.class,
            CombinedChsChiPicker.class,
            NaiveChiChsWordPicker.class);

    private final Class<? extends GegWordPicker> chsGegPickerClass;
    private final Class<? extends ChsCharPicker> gegChsPickerClass;
    private final Class<? extends ChsChiWordPicker> chsChiPickerClass;
    private final Class<? extends ChiChsWordPicker> chiChsPickerClass;

    PickerFactory(Class<? extends GegWordPicker> chsGegPickerClass,
                  Class<? extends ChsCharPicker> gegChsPickerClass,
                  Class<? extends ChsChiWordPicker> chsChiPickerClass,
                  Class<? extends ChiChsWordPicker> chiChsPickerClass) {
        this.chsGegPickerClass = chsGegPickerClass;
        this.gegChsPickerClass = gegChsPickerClass;
        this.chsChiPickerClass = chsChiPickerClass;
        this.chiChsPickerClass = chiChsPickerClass;
    }

    public GegWordPicker createChsToGeg(BigDict bigDict) {
        try {
            return chsGegPickerClass
                    .getDeclaredConstructor(BigDict.class, this.getClass())
                    .newInstance(bigDict, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ChsCharPicker createGegToChs(BigDict bigDict) {
        try {
            return gegChsPickerClass
                    .getDeclaredConstructor(BigDict.class, this.getClass())
                    .newInstance(bigDict, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ChsChiWordPicker createChsToChi(BigDict bigDict) {
        try {
            return chsChiPickerClass
                    .getDeclaredConstructor(BigDict.class, this.getClass())
                    .newInstance(bigDict, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ChiChsWordPicker createChiToChs(BigDict bigDict) {
        try {
            return chiChsPickerClass
                    .getDeclaredConstructor(BigDict.class, this.getClass())
                    .newInstance(bigDict, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
