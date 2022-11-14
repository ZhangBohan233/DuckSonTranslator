package trashsoftware.duckSonTranslator.wordPickerChsGeg;

public class Result {
    public static final Result NOT_FOUND = 
            new Result(null, null, 0, 0.0);
    
    public final String translated;
    public final String partOfSpeech;
    public final int matchLength;
    public final double precedence;

    Result(String translated, String partOfSpeech, int matchLength) {
        this(translated,
                partOfSpeech,
                matchLength,
                1.0 / translated.length());  // 默认的优先级是越短越好
    }

    Result(String translated, String partOfSpeech, int matchLength, double precedence) {
        this.translated = translated;
        this.partOfSpeech = partOfSpeech;
        this.matchLength = matchLength;
        this.precedence = precedence;
    }

    @Override
    public String toString() {
        return String.format("(%s)%s@%d", partOfSpeech, translated, matchLength);
    }
}
