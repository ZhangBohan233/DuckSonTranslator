package trashsoftware.duckSonTranslator.words;

import trashsoftware.duckSonTranslator.dict.BaseItem;
import trashsoftware.duckSonTranslator.dict.BigDictValue;

import java.util.*;

public class GegChsSearcher extends Searcher {
    
    protected GegChsSearcher(DuckSonDictionary parent) {
        super(parent, "geg", "chs");
    }

    @Override
    public List<WordResult> search(String text) {
        LinkedHashMap<String, WordResult> results = new LinkedHashMap<>();  // chs: results

        BaseItem baseItem = parent.baseDict.getByEng(text);
        if (baseItem != null) {
            reverseSearch(results, baseItem.eng, Map.of(baseItem.partOfSpeech, Set.of(baseItem.chs)), true);
        }
        BigDictValue wordMatch = parent.bigDict.getByEng(text, true);
        if (wordMatch != null) {
            reverseSearch(results, text, wordMatch.value, true);
        }

        return new ArrayList<>(results.values());
    }
    
    private void reverseSearch(LinkedHashMap<String, WordResult> results,
                               String engWord,
                               Map<String, Set<String>> chsPosDes, 
                               boolean hugeDict) {
        for (var entry : chsPosDes.entrySet()) {
            for (String chsWord : entry.getValue()) {
                WordResult result = results.computeIfAbsent(chsWord,
                        k -> new WordResult(engWord, chsWord, srcLang, dstLang, false));
                Map<String, LinkedHashSet<String>> engPosDes = chsToGegList(chsWord, hugeDict);
                result.addPosDescription(engPosDes);
            }
        }
    }

    private Map<String, LinkedHashSet<String>> chsToGegList(String chs, boolean hugeDict) {
        Map<String, LinkedHashSet<String>> engPosDes = new HashMap<>();
        BaseItem baseItem = parent.baseDict.getByChs(chs, 0);
        if (baseItem != null && baseItem.chs.equals(chs)) {
            engPosDes.put(baseItem.partOfSpeech, new LinkedHashSet<>(List.of(baseItem.eng)));
        }
        BigDictValue bdv = parent.bigDict.getByChs(chs, hugeDict);
        if (bdv != null) {
            for (var entry : bdv.value.entrySet()) {
                LinkedHashSet<String> des = engPosDes.computeIfAbsent(entry.getKey(),
                        k -> new LinkedHashSet<>());
                des.addAll(entry.getValue());
            }
        }

        return engPosDes;
    }
}
