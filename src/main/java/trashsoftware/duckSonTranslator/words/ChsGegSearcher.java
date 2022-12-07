package trashsoftware.duckSonTranslator.words;

import trashsoftware.duckSonTranslator.dict.BaseItem;
import trashsoftware.duckSonTranslator.dict.BigDictValue;

import java.util.*;

public class ChsGegSearcher extends Searcher {

    protected ChsGegSearcher(DuckSonDictionary parent) {
        super(parent, "chs", "geg");
    }

    @Override
    public List<WordResult> search(String text) {
        LinkedHashMap<String, WordResult> results = new LinkedHashMap<>();  // eng: results

        BaseItem baseItem = parent.baseDict.getByChs(text, 0);
        if (baseItem != null && baseItem.chs.equals(text)) {
            reverseSearch(results, baseItem.chs, Map.of(baseItem.partOfSpeech, Set.of(baseItem.eng)), true);
        }
        BigDictValue wordMatch = parent.bigDict.getByChs(text, true);
        if (wordMatch != null) {
            reverseSearch(results, text, wordMatch.value, true);
        }

        return new ArrayList<>(results.values());
    }

    private void reverseSearch(LinkedHashMap<String, WordResult> results,
                               String chsWord,
                               Map<String, Set<String>> engPosDes,
                               boolean hugeDict) {
        for (var entry : engPosDes.entrySet()) {
            for (String engWord : entry.getValue()) {
                WordResult result = results.computeIfAbsent(engWord,
                        k -> new WordResult(chsWord, engWord, srcLang, dstLang, false));
                Map<String, LinkedHashSet<String>> chsPosDes = gegToChsList(engWord, hugeDict);
                result.addPosDescription(chsPosDes);
            }
        }
    }

    private Map<String, LinkedHashSet<String>> gegToChsList(String geg, boolean hugeDict) {
        // 反向搜索
        Map<String, LinkedHashSet<String>> chsPosDes = new HashMap<>();
        BaseItem baseItem = parent.baseDict.getByEng(geg);
        if (baseItem != null) {
            chsPosDes.put(baseItem.partOfSpeech, new LinkedHashSet<>(List.of(baseItem.chs)));
        }
        BigDictValue bdv = parent.bigDict.getByEng(geg, hugeDict);
        if (bdv != null) {
            for (var entry : bdv.value.entrySet()) {
                LinkedHashSet<String> des = chsPosDes.computeIfAbsent(entry.getKey(),
                        k -> new LinkedHashSet<>());
                des.addAll(entry.getValue());
            }
        }

        return chsPosDes;
    }
}
