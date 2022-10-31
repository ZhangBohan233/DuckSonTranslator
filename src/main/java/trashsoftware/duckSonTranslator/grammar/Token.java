package trashsoftware.duckSonTranslator.grammar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Token {
    
    private String chs;
    private String eng;
    private String partOfSpeech;
    private Set<String> appliedTenses = new HashSet<>();
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
    
    public void applyTense(String tenseName) {
        if (!appliedTenses.contains(tenseName)) {
            appliedTenses.add(tenseName);
            switch (tenseName) {
                case "past":
                    applyPast();
                    break;
                case "belong":
                    applyBelong();
                    break;
                case "ing":
                    applyIng();
                    break;
                default:
                    break;
            }
        }
    }

    private void applyPast() {
        String eng = getEng();
        if (eng.endsWith("e")) {
            setEng(eng + "d");
        } else {
            setEng(eng + "ed");
        }
    }

    private void applyBelong() {
        String eng = getEng();
        if (eng.endsWith("s")) {
            setEng(eng + "'");
        } else {
            setEng(eng + "'s");
        }
    }

    private void applyIng() {
        String eng = getEng();
        if (eng.endsWith("e")) {
            setEng(eng.substring(0, eng.length() - 1) + "ing");
        } else {
            setEng(eng + "ing");
        }
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
