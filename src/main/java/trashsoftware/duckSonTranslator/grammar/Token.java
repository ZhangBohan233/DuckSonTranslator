package trashsoftware.duckSonTranslator.grammar;

import java.util.*;

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

    public Token(String eng) {
        this.eng = eng;
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
    
    public List<Token> applyTenseToChs(GrammarDict grammarDict) {
        List<Token> result = new ArrayList<>();
        for (String tense : appliedTenses) {
            GrammarEffect ge = grammarDict.tenseNameMap.get(tense);
            if (ge.effectiveIndex < 0) {
                return List.of(this, new Token(ge.tenseKeyWord, ge));
            } else {
                return List.of(new Token(ge.tenseKeyWord, ge), this);
            }
        }
        return result;
    }
    
//    private Token[] applyTenseToChs(String tenseName) {
//        switch (tenseName) {
//            case "past":
//                return new Token[]{
//                        new Token("", "了")
//                };
//            case "belong":
//                applyBelong();
//                break;
//            case "ing":
//                applyIng();
//                break;
//            default:
//                break;
//        }
//    }

    public boolean isUntranslatedEng() {
        return eng != null && chs == null && grammarEffect == null;
    }

    /**
     * Only for Geglish -> Chinese
     */
    public void addTense(String tenseName) {
        appliedTenses.add(tenseName);
    }

    public Set<String> getTenses() {
        return appliedTenses;
    }

    public String[][] getPossibleBaseEngForm() {
        int len = eng.length();
        if (eng.endsWith("ed")) {
            if (len > 2) return new String[][]{
                    {eng.substring(0, len - 2), "past"},
                    {eng.substring(0, len - 1), "past"}
            };
            else return new String[][]{
                    {eng.substring(0, len - 1), "past"}
            };
        }
        if (eng.endsWith("ing")) {
            if (len > 3) return new String[][]{
                    {eng.substring(0, len - 3), "ing"},
                    {eng.substring(0, len - 3) + "e", "ing"}
            };
//            else return new String[][]{
//                    {eng.substring(0, len - 2) + "e", "ing"}
//            };
        }
        if (eng.endsWith("'s")) {
            if (len > 2) return new String[][]{{eng.substring(0, len - 2), "belong"}};
        }
        if (eng.endsWith("'")) {
            if (len > 1) return new String[][]{{eng.substring(0, len - 1), "belong"}};
        }
        return null;
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

    public void setEng(String eng) {
        this.eng = eng;
    }

    public void setChs(String chs) {
        this.chs = chs;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public GrammarEffect getGrammarEffect() {
        return grammarEffect;
    }

    @Override
    public String toString() {
        return "Token{" + chs + ", " + eng + ", " + partOfSpeech + '}';
    }
}
