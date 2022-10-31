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
}
