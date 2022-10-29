package trashsoftware.duckSonTranslator.grammar;

public class Token {
    
    private String chs;
    private String eng;
    private String partOfSpeech;
    private GrammarEffect grammarEffect;
    
    public Token(String chs, String eng, String partOfSpeech) {
        this.chs = chs;
        this.eng = eng;
        this.partOfSpeech = partOfSpeech;
    }

    public Token(String chs, GrammarEffect effect) {
        this.chs = chs;
        this.grammarEffect = effect;
    }
    
    public boolean isActual() {
        return this.grammarEffect == null;
    }
    
    public boolean isEffect() {
        return this.grammarEffect != null;
    }

    public String getChs() {
        return chs;
    }

    public String getEng() {
        return eng;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setEng(String eng) {
        this.eng = eng;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public GrammarEffect getGrammarEffect() {
        return grammarEffect;
    }

    @Override
    public String toString() {
        return "Token{" +
                "original='" + chs + '\'' +
                ", translated='" + eng + '\'' +
                ", partOfSpeech='" + partOfSpeech + '\'' +
                '}';
    }
}
