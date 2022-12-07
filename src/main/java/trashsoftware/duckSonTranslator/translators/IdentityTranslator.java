package trashsoftware.duckSonTranslator.translators;

import trashsoftware.duckSonTranslator.result.ResultToken;
import trashsoftware.duckSonTranslator.result.TranslationResult;

public class IdentityTranslator extends Translator {
    
    public IdentityTranslator(DuckSonTranslator parent) {
        super(parent);
    }

    @Override
    public TranslationResult translate(String text) {
        TranslationResult result = new TranslationResult(text);
        for (int i = 0; i < text.length(); i++) {
            result.add(new ResultToken(String.valueOf(text.charAt(i)), i, 1));
        }
        return result;
    }
}
