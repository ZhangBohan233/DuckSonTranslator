package trashsoftware.duckSonTranslator.grammar;

import java.util.*;

public class Token {

    private String chs;
    private String eng;
    private String engAfterTense;
    private String partOfSpeech;
    final Set<String> appliedTenses = new HashSet<>();
    private GrammarEffect grammarEffect;
    private boolean applied = false;  // 语法token是否已被apply
    
    // 只在grammar token里用, 记载哪个actual token被这个grammar token上了
    private Token tokenAppliedThisGrammar;
    
    private int posInOrig;
    private int lengthInOrig;

    public Token(String chs, String eng, String partOfSpeech) {
        this.chs = chs;
        this.eng = eng;
        this.partOfSpeech = partOfSpeech;
    }

    public Token(String chs, String eng, String partOfSpeech, int posInOrig, int lengthInOrig) {
        this.chs = chs;
        this.eng = eng;
        this.partOfSpeech = partOfSpeech;
        this.posInOrig = posInOrig;
        this.lengthInOrig = lengthInOrig;
    }

    public Token(String chs, String eng, GrammarEffect effect) {
        this.chs = chs;
        this.grammarEffect = effect;
        this.eng = eng;
        this.partOfSpeech = "det";
    }

    public Token(String chs, String eng, GrammarEffect effect, int posInOrig, int lengthInOrig) {
        this.chs = chs;
        this.grammarEffect = effect;
        this.eng = eng;
        this.partOfSpeech = "det";
        this.posInOrig = posInOrig;
        this.lengthInOrig = lengthInOrig;
    }

    public Token(String eng) {
        this.eng = eng;
        this.engAfterTense = eng;
    }

    public Token(String eng, int posInOrig, int lengthInOrig) {
        this.eng = eng;
        this.engAfterTense = eng;
        this.posInOrig = posInOrig;
        this.lengthInOrig = lengthInOrig;
    }

    public int getLengthInOrig() {
        return lengthInOrig;
    }

    public int getPosInOrig() {
        return posInOrig;
    }

    public void setPosInOrig(int posInOrig, int lengthInOrig) {
        this.posInOrig = posInOrig;
        this.lengthInOrig = lengthInOrig;
    }

    public boolean isGrammarApplied() {
        return applied;
    }
    
    public void setGrammarApplied(Token tokenAppliedThisGrammar) {
        this.applied = true;
        this.tokenAppliedThisGrammar = tokenAppliedThisGrammar;
    }

    public Token getTokenAppliedThisGrammar() {
        return tokenAppliedThisGrammar;
    }

    public void applyTense(String tenseName) {
        if (!appliedTenses.contains(tenseName)) {
            appliedTenses.add(tenseName);
            switch (tenseName) {
                case "past":
                    applyPast();
                    break;
                case "belong":
                    applyBelong(true);
                    break;
                case "home":
                    applyBelong(false);
                    break;
                case "plural":
                    applyPlural();
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
        setEngAfterTense(eng + "ed");
    }

    private void applyBelong(boolean checkVerb) {
        String eng = getEng();
        if (partOfSpeech.equals("v")) {
            if (checkVerb) {
                setEngAfterTense(eng + "en");
            }
        } else {
            setEngAfterTense(eng + "'s");
        }
    }

    private void applyIng() {
        String eng = getEng();
        setEngAfterTense(eng + "ing");
    }
    
    private void applyBetter() {
        String eng = getEng();
        setEngAfterTense(eng + "er");
    }
    
    private void applyPlural() {
        String eng = getEng();
        if (!eng.endsWith("s")) {
            setEngAfterTense(eng + "s");
        }
    }
    
    private void applyBest() {
        String eng = getEng();
        setEngAfterTense(eng + "est");
    }
    
    public List<Token> applyTenseToChs(GrammarDict grammarDict) {
        List<Token> result = new ArrayList<>();
        for (String tense : appliedTenses) {
            GrammarEffect ge = grammarDict.tenseNameMap.get(tense);
            if (ge.effectiveIndex < 0) {
                return List.of(this, new Token(ge.tenseKeyWord, ge.engDirect, ge, posInOrig, lengthInOrig));
            } else {
//                System.out.println(eng + " " + engAfterTense);
                return List.of(new Token(ge.tenseKeyWord, ge.engDirect, ge, posInOrig, lengthInOrig), this);
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
            return new String[][]{{eng.substring(0, len - 2), "past"}};
        }
        if (eng.endsWith("en")) {
            if (len > 2) return new String[][]{{eng.substring(0, len - 2), "belong"}};
        }
        if (eng.endsWith("ing")) {
            if (len > 3) return new String[][]{
                    {eng.substring(0, len - 3), "ing"},
            };
        }
        if (eng.endsWith("'s")) {
            if (len > 2) return new String[][]{{eng.substring(0, len - 2), "belong"}};
        }
        if (eng.endsWith("s")) {
            if (len > 1) return new String[][]{{eng.substring(0, len - 1), "plural"}};
        }
        if (eng.endsWith("er")) {
            if (len > 2) return new String[][]{
                    {eng.substring(0, len - 2), "better"}
            };
        }
        if (eng.endsWith("est")) {
            if (len > 3) return new String[][]{
                    {eng.substring(0, len - 3), "best"},
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
    
    public boolean isTreatedAsActual() {
        return isActual() || !isGrammarApplied();
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
