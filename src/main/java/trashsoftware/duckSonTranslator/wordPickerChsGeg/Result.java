package trashsoftware.duckSonTranslator.wordPickerChsGeg;

public class Result {
    public final String translated;
    public final String partOfSpeech;
    public final int matchLength;

    Result(String translated, String partOfSpeech, int matchLength) {
        this.translated = translated;
        this.partOfSpeech = partOfSpeech;
        this.matchLength = matchLength;
    }

    @Override
    public String toString() {
        return String.format("(%s)%s@%d", partOfSpeech, translated, matchLength);
    }
}
