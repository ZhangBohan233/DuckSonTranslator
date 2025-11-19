package trashsoftware.duckSonTranslator.translators;

import trashsoftware.duckSonTranslator.dict.BaseItem;
import trashsoftware.duckSonTranslator.grammar.Token;
import trashsoftware.duckSonTranslator.wordPickers.ResultFromLatin;

public class ChiToChsTranslator extends StdLatinToChs {
    protected ChiToChsTranslator(DuckSonTranslator parent) {
        super(parent, "chi", "chs");
    }

    @Override
    protected void translateToken(Token token) {
        // 检查baseDict
        String eng = token.getEng();
        BaseItem baseItem = parent.baseDict.getByEng(eng);
        if (baseItem != null) {
            token.setChs(baseItem.chs);
            token.setPartOfSpeech(baseItem.partOfSpeech);
            return;
        }
        String[][] possibleForms = token.getPossibleBaseEngForm();
        if (possibleForms != null) {
            for (String[] engTense : possibleForms) {
                baseItem = parent.baseDict.getByEng(engTense[0]);
                if (baseItem != null) {
                    token.setOriginalEng(engTense[0]);
                    token.setChs(baseItem.chs);
                    token.setPartOfSpeech(baseItem.partOfSpeech);
                    token.addTense(engTense[1]);
                    return;
                }
            }
        }

        // 检查bigDict
        ResultFromLatin chsDirect = parent.getChiToChsPicker().translate(eng);
        if (chsDirect != null) {
            token.setChs(chsDirect.translated);
            token.setPartOfSpeech(chsDirect.partOfSpeech);
            return;
        }
        if (possibleForms != null) {
            for (String[] engTense : possibleForms) {
                chsDirect = parent.getChiToChsPicker().translate(engTense[0]);
                if (chsDirect != null) {
                    token.setOriginalEng(engTense[0]);
                    token.setChs(chsDirect.translated);
                    token.setPartOfSpeech(chsDirect.partOfSpeech);
                    token.addTense(engTense[1]);
                    return;
                }
            }
        }
    }
}
