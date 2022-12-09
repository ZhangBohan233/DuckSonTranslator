package trashsoftware.duckSonTranslator.words;

import trashsoftware.duckSonTranslator.dict.BaseItem;
import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.BigDictValue;
import trashsoftware.duckSonTranslator.translators.StdLatinToChs;

import java.util.*;

public class ChsGegSearcher extends Searcher {

    protected ChsGegSearcher(DuckSonDictionary parent) {
        super(parent, "chs", "geg");
    }

    @Override
    protected List<WordResult> searchByText(String text) {
        LinkedHashMap<String, WordResult> results = new LinkedHashMap<>();  // eng: results

        searchBySubstring(results, text, false);

        if (parent.getOptions().isUseSameSoundChar()) {
            String[] sameSound = sameSoundCombinations(text);
            for (String sameSoundWord : sameSound) {
                if (!sameSoundWord.equals(text)) {
                    searchBySubstring(results, sameSoundWord, true);
                }
            }
        }

        return new ArrayList<>(results.values());
    }

    private void searchBySubstring(LinkedHashMap<String, WordResult> results,
                                   String word,
                                   boolean isSameSound) {
        if (parent.getOptions().isUseBaseDict()) {
            BaseItem baseItem = parent.baseDict.getByChs(word, 0);  // Base dict只搜本体，不搜substring，因为必要性不大
            if (baseItem != null && baseItem.chs.equals(word)) {
                reverseSearch(word, results, baseItem.chs, Map.of(baseItem.partOfSpeech, Set.of(baseItem.eng)), isSameSound);
            }
        }
        BigDict.WordMatch wordMatch = parent.bigDict.findWordMatchesByChs(word, useHugeDict, true);
        if (wordMatch != null) {
            for (var entry : wordMatch.matches.entrySet()) {
                reverseSearch(word, results, entry.getKey(), entry.getValue().value, isSameSound);
            }
        }
    }

    private void reverseSearch(String searchedOrig, 
                               LinkedHashMap<String, WordResult> results,
                               String chsWord,
                               Map<String, Set<String>> engPosDes,
                               boolean isSameSound) {
        for (var entry : engPosDes.entrySet()) {
            for (String engWord : entry.getValue()) {
                WordResult result = results.computeIfAbsent(engWord,
                        k -> new WordResult(searchedOrig, chsWord, engWord, srcLang, dstLang, isSameSound));
                Map<String, LinkedHashSet<String>> chsPosDes = gegToChsList(engWord);
                result.addPosDescription(chsPosDes);
            }
        }
    }

    private Map<String, LinkedHashSet<String>> gegToChsList(String geg) {
        // 反向搜索
        Map<String, LinkedHashSet<String>> chsPosDes = new HashMap<>();
        if (parent.getOptions().isUseBaseDict()) {
            BaseItem baseItem = parent.baseDict.getByEng(geg);
            if (baseItem != null) {
                chsPosDes.put(baseItem.partOfSpeech, new LinkedHashSet<>(List.of(baseItem.chs)));
            }
        }
        BigDictValue bdv = parent.bigDict.getByEng(geg, isUseHugeDict());
        if (bdv != null) {
            for (var entry : bdv.value.entrySet()) {
                LinkedHashSet<String> des = chsPosDes.computeIfAbsent(entry.getKey(),
                        k -> new LinkedHashSet<>());
                des.addAll(entry.getValue());
            }
        }

        return chsPosDes;
    }

    private String[] sameSoundCombinations(String word) {
        List<String[]>[] possibles = new List[word.length()];
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            List<Character> sameSounds = parent.pinyinDict.getSameSoundChsChars(c, parent.getOptions().isChongqingMode());
            List<String[]> possiblesAtI = new ArrayList<>();
            for (Character cc : sameSounds) {
                possiblesAtI.add(new String[]{String.valueOf(cc)});
            }
            possibles[i] = possiblesAtI;
        }

        String[][][] comb = StdLatinToChs.makeCombinations(possibles);
        String[] res = new String[comb.length];

        for (int i = 0; i < res.length; i++) {
            StringBuilder builder = new StringBuilder();
            for (String[] arr : comb[i]) {
                builder.append(arr[0]);
            }
            res[i] = builder.toString();
        }

        return res;
    }
}
