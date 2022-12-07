package trashsoftware.duckSonTranslator.wordPickers;

public class ResultFromLatin extends Result {
    public static final ResultFromLatin NOT_FOUND = new ResultFromLatin(null, null);
    
    public final String translated;
    public final String partOfSpeech;

    public ResultFromLatin(String translated, String partOfSpeech) {
        this.translated = translated;
        this.partOfSpeech = partOfSpeech;
    }

    @Override
    public String toString() {
        return String.format("(%s)%s", partOfSpeech, translated);
    }
}
