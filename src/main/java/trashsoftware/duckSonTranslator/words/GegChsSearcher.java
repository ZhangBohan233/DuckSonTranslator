package trashsoftware.duckSonTranslator.words;

import trashsoftware.duckSonTranslator.dict.BaseItem;
import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.BigDictValue;

import java.util.*;

public class GegChsSearcher extends Searcher {
    
    protected GegChsSearcher(DuckSonDictionary parent) {
        super(parent, "geg", "chs");
    }

    @Override
    protected List<WordResult> searchByText(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        LinkedHashMap<String, WordResult> results = new LinkedHashMap<>();  // chs: results

        searchBySubstring(results, lower);

        return new ArrayList<>(results.values());
    }
    
    private void searchBySubstring(LinkedHashMap<String, WordResult> results,
                                   String engWord) {
        if (parent.getOptions().isUseBaseDict()) {
            BaseItem baseItem = parent.baseDict.getByEng(engWord);
            if (baseItem != null) {
                reverseSearch(engWord, results, baseItem.eng, Map.of(baseItem.partOfSpeech, Set.of(baseItem.chs)), WordResultType.EXACT);
            }
        }
        BigDict.WordMatch wordMatches = parent.bigDict.findSubstringMatchesByEng(engWord, parent.options.isUseHugeDict());
        if (wordMatches != null) {
            for (var entry : wordMatches.matches.entrySet()) {
                String gotEngWord = entry.getKey();
                WordResultType type;
                if (engWord.equals(gotEngWord)) type = WordResultType.EXACT;
                else if (gotEngWord.startsWith(engWord)) type = WordResultType.PREFIX;
                else if (gotEngWord.contains(engWord)) type = WordResultType.SUBSTRING;
                else type = WordResultType.ROUGH;
                reverseSearch(engWord, results, entry.getKey(), entry.getValue().value, type);
            }
        }
    }
    
    private void reverseSearch(String searchedOrig, 
                               LinkedHashMap<String, WordResult> results,
                               String engWord,
                               Map<String, Set<String>> chsPosDes,
                               WordResultType type) {
        for (var entry : chsPosDes.entrySet()) {
            for (String chsWord : entry.getValue()) {
                WordResult result = results.computeIfAbsent(chsWord,
                        k -> new WordResult(searchedOrig, engWord, chsWord, srcLang, dstLang, type));
                Map<String, LinkedHashSet<String>> engPosDes = chsToGegList(chsWord);
                result.addPosDescription(engPosDes);
            }
        }
    }

    private Map<String, LinkedHashSet<String>> chsToGegList(String chs) {
        Map<String, LinkedHashSet<String>> engPosDes = new HashMap<>();
        if (parent.getOptions().isUseBaseDict()) {
            BaseItem baseItem = parent.baseDict.getByChs(chs, 0);
            if (baseItem != null && baseItem.chs.equals(chs)) {
                engPosDes.put(baseItem.partOfSpeech, new LinkedHashSet<>(List.of(baseItem.eng)));
            }
        }
        BigDictValue bdv = parent.bigDict.getByChs(chs, parent.options.isUseHugeDict());
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
