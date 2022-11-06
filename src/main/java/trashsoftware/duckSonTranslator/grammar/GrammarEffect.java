package trashsoftware.duckSonTranslator.grammar;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GrammarEffect {

    public final String tenseKeyWord;  // 中文关键字，最关键的一个就行
    public final String engDirect;
    public final String tenseName;
    public final int effectiveIndex;
    public final Set<String> effectivePos;
    public final Map<String, String[][]> specialPreComb;
    public final Map<String, String[][]> specialPostComb;

    public final Map<String[][], String> combsEngChs = new HashMap<>();

    public GrammarEffect(
            String tenseKeyWord,
            String engDirect,
            String tenseName,
            int effectiveIndex,
            Set<String> effectivePos,
            Map<String, String[][]> specialPreComb,
            Map<String, String[][]> specialPostComb) {
        this.tenseKeyWord = tenseKeyWord;
        this.engDirect = engDirect;
        this.tenseName = tenseName;
        this.effectiveIndex = effectiveIndex;
        this.effectivePos = effectivePos;
        this.specialPreComb = specialPreComb;
        this.specialPostComb = specialPostComb;

        makeReverseCombs(specialPreComb, false);
        makeReverseCombs(specialPostComb, true);
    }

    private void makeReverseCombs(Map<String, String[][]> orig, boolean post) {
        for (Map.Entry<String, String[][]> entry : orig.entrySet()) {
            String[][] val = entry.getValue();
            String word = post ? (tenseKeyWord + entry.getKey()) : (entry.getKey() + tenseKeyWord);
            combsEngChs.put(val, word);
        }
    }
    
    public boolean isPost(String chsWord) {
        String subBefore = chsWord.substring(0, chsWord.length() - tenseKeyWord.length());
        if (specialPreComb.containsKey(subBefore)) return false;

        String subAfter = chsWord.substring(chsWord.length() - tenseKeyWord.length());
        if (specialPostComb.containsKey(subAfter)) return true;
        
        throw new RuntimeException();
    }
}
