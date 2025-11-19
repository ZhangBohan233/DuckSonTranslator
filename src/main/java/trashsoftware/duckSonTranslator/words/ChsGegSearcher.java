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
//        text = text.toUpperCase(Locale.ROOT);  // 以防有中英混杂的 -- 在同音字里面用大小写替换了
        LinkedHashMap<String, WordResult> results = new LinkedHashMap<>();  // eng: results

        searchPinyin(results, text);
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
    
    private static String produceReadablePolyphone(String majorPinyin, String[] allPinyin) {
        if (majorPinyin == null) {
            if (allPinyin == null || allPinyin.length == 0) return "";
            
            if (allPinyin.length == 1) return allPinyin[0];
            else {
                return allPinyin[0] + "(" + String.join(", ", Arrays.copyOfRange(allPinyin, 1, allPinyin.length)) + ")";
            }
        } else {
            if (allPinyin == null) return majorPinyin;
            if (allPinyin.length == 1 && majorPinyin.equals(allPinyin[0])) return majorPinyin;
            else {
                List<String> poly = new ArrayList<>();
                for (String ap : allPinyin) {
                    if (!majorPinyin.equals(ap)) poly.add(ap);
                }
                return majorPinyin + "(" + String.join(", ", poly) + ")";
            }
        }
    }

    /**
     * 直接加入汉字-拼音结果
     */
    private void searchPinyin(LinkedHashMap<String, WordResult> results,
                              String word) {
        List<String> puTongHua = new ArrayList<>();
        List<String> cqPin = new ArrayList<>();
        for (char c : word.toCharArray()) {
            String[] pinyin = parent.pinyinDict.getPinyinByChs(c);
            String[] fullPin = parent.pinyinDict.getFullPinyinByChs(String.valueOf(c));
            if (pinyin == null) {
                if (fullPin == null) {
                    puTongHua.add(c + "");
                    cqPin.add(c + "");
                } else {
                    puTongHua.add(produceReadablePolyphone(null, fullPin));
                    cqPin.add(c + "");
                }
            } else {
                puTongHua.add(produceReadablePolyphone(pinyin[2], fullPin));  // 这是真拼音
                cqPin.add(pinyin[1]);
            }
        }
        String pth = String.join(" ", puTongHua);
        String cq = String.join(" ", cqPin);
        WordResult result = new WordResult(word, word, word, "chs", "pin", WordResultType.PINYIN);
        result.addPosDescription(Map.of("pinyin", new LinkedHashSet<>(Set.of(pth))));
        result.addPosDescription(Map.of("cqPin", new LinkedHashSet<>(Set.of(cq))));
        results.put(word, result);
    }

    private void searchBySubstring(LinkedHashMap<String, WordResult> results,
                                   String word,
                                   boolean isSameSound) {
        if (parent.getOptions().isUseBaseDict()) {
            BaseItem baseItem = parent.baseDict.getByChs(word, 0, parent.getOptions());  // Base dict只搜本体，不搜substring，因为必要性不大
            if (baseItem != null && baseItem.chs.equals(word)) {
                reverseSearch(word, results, baseItem.chs, Map.of(baseItem.partOfSpeech,
                                Set.of(baseItem.eng)),
                        isSameSound ? WordResultType.HOMOPHONE : WordResultType.EXACT);
            }
        }
        BigDict.WordMatch substringMatch = parent.bigDict.findSubstringMatchesByChs(word,
                parent.options.isUseHugeDict());
        if (substringMatch != null) {
            for (var entry : substringMatch.matches.entrySet()) {
                String gotChsWord = entry.getKey();
                WordResultType type;
                if (isSameSound) type = WordResultType.HOMOPHONE;
                else if (word.equals(gotChsWord)) type = WordResultType.EXACT;
                else if (gotChsWord.startsWith(word)) type = WordResultType.PREFIX;
                else if (gotChsWord.contains(word)) type = WordResultType.SUBSTRING;
                else type = WordResultType.ROUGH;

                reverseSearch(word, results, gotChsWord, entry.getValue().value, type);
            }
        }
    }

    private void reverseSearch(String searchedOrig,
                               LinkedHashMap<String, WordResult> results,
                               String chsWord,
                               Map<String, Set<String>> engPosDes,
                               WordResultType type) {
        for (var entry : engPosDes.entrySet()) {
            for (String engWord : entry.getValue()) {
                WordResult result = results.computeIfAbsent(engWord,
                        k -> new WordResult(searchedOrig, chsWord, engWord, srcLang, dstLang, type));
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
        BigDictValue bdv = parent.bigDict.getByEng(geg, parent.options.isUseHugeDict());
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
                if (parent.translatePossible(cc)) {
                    possiblesAtI.add(new String[]{String.valueOf(cc)});
                }
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
