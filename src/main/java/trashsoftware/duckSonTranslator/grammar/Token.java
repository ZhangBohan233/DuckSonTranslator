package trashsoftware.duckSonTranslator.grammar;

import trashsoftware.duckSonTranslator.translators.NoSuchWordException;

import java.util.*;

public class Token {
    
    public static final Token SPACE_TOKEN = new Token(" ", "", "etc");

    private String chs;
    private String eng;
    private String engAfterTense;
    private String partOfSpeech;
    private final Set<String> appliedTenses = new HashSet<>();
    private GrammarEffect grammarEffect;
    private boolean applied = false;  // 语法token是否已被apply

    public Token(String chs, String eng, String partOfSpeech) {
        this.chs = chs;
        this.eng = eng;
        this.partOfSpeech = partOfSpeech;
    }

    public Token(String chs, String eng, GrammarEffect effect) {
        this.chs = chs;
        this.grammarEffect = effect;
        this.eng = eng;
        this.partOfSpeech = "det";
    }

    public Token(String eng) {
        this.eng = eng;
        this.engAfterTense = eng;
    }

    public boolean isGrammarApplied() {
        return applied;
    }
    
    public void setGrammarApplied() {
        this.applied = true;
    }

    public void applyTense(String tenseName) {
        if (!appliedTenses.contains(tenseName)) {
            appliedTenses.add(tenseName);
            switch (tenseName) {
                case "past":
                    applyPast();
                    break;
                case "belong":
                case "home":
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
            setEngAfterTense(eng + "d");
        } else {
            setEngAfterTense(eng + "ed");
        }
    }

    private void applyBelong() {
        String eng = getEng();
        if (eng.endsWith("s")) {
            setEngAfterTense(eng + "'");
        } else {
            setEngAfterTense(eng + "'s");
        }
    }

    private void applyIng() {
        String eng = getEng();
        if (eng.endsWith("e")) {
            setEngAfterTense(eng.substring(0, eng.length() - 1) + "ing");
        } else {
            setEngAfterTense(eng + "ing");
        }
    }
    
    private void applyBetter() {
        String eng = getEng();
        if (eng.endsWith("e")) {
            setEngAfterTense(eng + "r");
        } else if (eng.endsWith("y")) {
            setEngAfterTense(eng.substring(0, eng.length() - 1) + "ier");
        }else {
            setEngAfterTense(eng + "er");
        }
    }
    
    private void applyBest() {
        String eng = getEng();
        if (eng.endsWith("es")) {
            setEngAfterTense(eng + "t");
        } else if (eng.endsWith("e")) {
            setEngAfterTense(eng + "st");
        } else if (eng.endsWith("y")) {
            setEngAfterTense(eng.substring(0, eng.length() - 1) + "iest");
        } else {
            setEngAfterTense(eng + "est");
        }
    }

    public Token applyTenseToChsSingleRes(GrammarDict grammarDict) {
        for (String tense : appliedTenses) {
            GrammarEffect ge = grammarDict.tenseNameMap.get(tense);
            if (ge.effectiveIndex < 0) {
                return new Token(this.chs + ge.tenseKeyWord, ge.engDirect, ge);
//                return List.of(this, new Token(ge.tenseKeyWord, ge.engDirect, ge));
            } else {
//                return List.of(new Token(ge.tenseKeyWord, ge.engDirect, ge), this);
                return new Token(ge.tenseKeyWord + this.chs, ge.engDirect, ge);
            }
        }
        throw new NoSuchWordException();
    }
    
    public List<Token> applyTenseToChs(GrammarDict grammarDict) {
        List<Token> result = new ArrayList<>();
        for (String tense : appliedTenses) {
            GrammarEffect ge = grammarDict.tenseNameMap.get(tense);
            if (ge.effectiveIndex < 0) {
                return List.of(this, new Token(ge.tenseKeyWord, ge.engDirect, ge));
            } else {
                return List.of(new Token(ge.tenseKeyWord, ge.engDirect, ge), this);
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

    /**
     * 结合上面这个用的，给译中
     */
    public void setOriginalEng(String eng) {
        this.eng = eng;
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
        return engAfterTense == null ? eng : engAfterTense;
    }

    public String getEngAfterTense() {
        return engAfterTense;
    }

    public String getOrigEng() {
        return eng;
    }

    public void setEngAfterTense(String eng) {
        this.engAfterTense = eng;
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
        return "Token{" + chs + ", " + eng + "(" + engAfterTense + ")" + ", " + partOfSpeech + '}';
    }
}
