package trashsoftware.duckSonTranslator.translators;

import trashsoftware.duckSonTranslator.result.ResultToken;
import trashsoftware.duckSonTranslator.result.TranslationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 中介型翻译器
 */
public abstract class IntermediateTranslator extends Translator {
    
    private final Translator t1;
    private final Translator t2;
    
    protected IntermediateTranslator(DuckSonTranslator parent, Translator t1, Translator t2) {
        super(parent);
        
        this.t1 = t1;
        this.t2 = t2;
    }

    @Override
    public TranslationResult translate(String text) {
        TranslationResult t1Result = t1.translate(text);
        String t1outString = t1Result.toString();
        TranslationResult t2Result = t2.translate(t1outString);
        
        TranslationResult result = new TranslationResult(text);
        for (ResultToken token : t2Result.getResultTokens()) {
            List<int[]> rangesInT1out = token.getOrigRanges();
            List<int[]> rangesInOrig = new ArrayList<>();
            for (int[] range : rangesInT1out) {
                List<ResultToken> tokensInT1out = t1Result.findTokensInRange(range[0], range[1]);
                for (ResultToken rt : tokensInT1out) {
                    rangesInOrig.addAll(rt.getOrigRanges());
                }
            }
            ResultToken newToken = new ResultToken(token.translated, rangesInOrig);
            result.add(newToken);
//            token.getOrigRanges()
        }
        
        return result;
    }
}
