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
                case "better":
                    applyBetter();
                    break;
                case "best":
                    applyBest();
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
    
    private void applyBetter() {
        String eng = getEng();
        if (eng.endsWith("e")) {
            setEng(eng + "r");
        } else if (eng.endsWith("y")) {
            setEng(eng.substring(0, eng.length() - 1) + "ier");
        }else {
            setEng(eng + "er");
        }
    }
    
    private void applyBest() {
        String eng = getEng();
        if (eng.endsWith("es")) {
            setEng(eng + "t");
        } else if (eng.endsWith("e")) {
            setEng(eng + "st");
        } else if (eng.endsWith("y")) {
            setEng(eng.substring(0, eng.length() - 1) + "iest");
        } else {
            setEng(eng + "est");
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
        if (eng.endsWith("ier")) {
            if (len > 3) return new String[][]{
                    {eng.substring(0, len - 3) + "y", "better"},
                    {eng.substring(0, len - 2), "better"},
                    {eng.substring(0, len - 1), "better"}
            };
        }
        if (eng.endsWith("er")) {
            if (len > 2) return new String[][]{
                    {eng.substring(0, len - 2), "better"},
                    {eng.substring(0, len - 1), "better"}
            };
        }
        if (eng.endsWith("iest")) {
            if (len > 4) return new String[][]{
                    {eng.substring(0, len - 4) + "y", "best"},
                    {eng.substring(0, len - 3), "best"},
                    {eng.substring(0, len - 2), "best"},
                    {eng.substring(0, len - 1), "best"},
            };
        }
        if (eng.endsWith("est")) {
            if (len > 3) return new String[][]{
                    {eng.substring(0, len - 3), "best"},
                    {eng.substring(0, len - 2), "best"},
                    {eng.substring(0, len - 1), "best"}
            };
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
