package trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.wordPickers.Result;

public class ResultFromChs extends Result {
    public static final ResultFromChs NOT_FOUND = 
            new ResultFromChs(null, null, 0, 0.0, true);
    
    public final String translated;
    public final String partOfSpeech;
    public final int matchLength;
    public final double precedence;
    public final boolean strong;  // 是不是好的匹配

    public ResultFromChs(String translated, String partOfSpeech, int matchLength) {
        this(translated,
                partOfSpeech,
                matchLength,
                1.0 / translated.length());  // 默认的优先级是越短越好
    }

    ResultFromChs(String translated, String partOfSpeech, int matchLength, double precedence) {
        this(translated, partOfSpeech, matchLength, precedence, true);
    }

    ResultFromChs(String translated, String partOfSpeech, int matchLength, double precedence, boolean strong) {
        this.translated = translated;
        this.partOfSpeech = partOfSpeech;
        this.matchLength = matchLength;
        this.precedence = precedence;
        this.strong = strong;
    }

    @Override
    public String toString() {
        return String.format("(%s)%s@%d", partOfSpeech, translated, matchLength);
    }
}
