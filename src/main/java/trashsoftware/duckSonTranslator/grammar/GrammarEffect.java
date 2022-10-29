package trashsoftware.duckSonTranslator.grammar;

import java.util.Map;
import java.util.Set;

public class GrammarEffect {
    
    public final String tenseName;
    public final int effectiveIndex;
    public final Set<String> effectivePos;
    public final Map<String, String[][]> specialPreComb;
    public final Map<String, String[][]> specialPostComb;
    
    public GrammarEffect(
            String tenseName,
            int effectiveIndex,
            Set<String> effectivePos,
            Map<String, String[][]> specialPreComb,
            Map<String, String[][]> specialPostComb) {
        this.tenseName = tenseName;
        this.effectiveIndex = effectiveIndex;
        this.effectivePos = effectivePos;
        this.specialPreComb = specialPreComb;
        this.specialPostComb = specialPostComb;
    }
    
    public void applyTo(Token token) {
        switch (tenseName) {
            case "past":
                applyPast(token);
                break;
            case "belong":
                applyBelong(token);
                break;
            case "ing":
                applyIng(token);
                break;
            default:
                break;
        }
    }
    
    private void applyPast(Token token) {
        String eng = token.getEng();
        if (eng.endsWith("e")) {
            token.setEng(eng + "d");
        } else {
            token.setEng(eng + "ed");
        }
    }
    
    private void applyBelong(Token token) {
        String eng = token.getEng();
        if (eng.endsWith("s")) {
            token.setEng(eng + "'");
        } else {
            token.setEng(eng + "'s");
        }
    }
    
    private void applyIng(Token token) {
        String eng = token.getEng();
        token.setEng(eng + "ing");
//        if (eng.endsWith("e")) {
//            token.setEng(eng + "d");
//        } else {
//            token.setEng(eng + "ed");
//        }
    }
}
