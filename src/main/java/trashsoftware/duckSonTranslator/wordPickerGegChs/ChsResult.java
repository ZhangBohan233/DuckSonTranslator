package trashsoftware.duckSonTranslator.wordPickerGegChs;

public class ChsResult {
    public static final ChsResult NOT_FOUND = new ChsResult(null, null);
    
    public final String translated;
    public final String partOfSpeech;

    public ChsResult(String translated, String partOfSpeech) {
        this.translated = translated;
        this.partOfSpeech = partOfSpeech;
    }

    @Override
    public String toString() {
        return String.format("(%s)%s", partOfSpeech, translated);
    }
}
