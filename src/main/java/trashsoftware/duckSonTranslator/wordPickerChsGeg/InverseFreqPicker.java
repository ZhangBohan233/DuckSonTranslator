package trashsoftware.duckSonTranslator.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.BigDictValue;
import trashsoftware.duckSonTranslator.trees.Trie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InverseFreqPicker extends WordPicker {
    protected InverseFreqPicker(BigDict bigDict) {
        super(bigDict);
    }

    @Override
    public Result translateWord(String sentence) {
        Trie.Get<BigDictValue> results = bigDict.getChsEngTrie().get(sentence);
        if (results.matchLength == 0) return null;
        Map<String, Map<String, Integer>> freqMap = new HashMap<>();  // 英文: {词性: 频次}
        for (Map.Entry<String, Trie.Match<BigDictValue>> entry : results.value.entrySet()) {
            for (Map.Entry<String, List<String>> posDes : entry.getValue().value.value.entrySet()) {
                String pos = posDes.getKey();
                for (String des : posDes.getValue()) {
                    Map<String, Integer> posFreq = freqMap.get(des);
                    if (posFreq == null) {
                        posFreq = new HashMap<>();
                        posFreq.put(pos, 1);
                        freqMap.put(des, posFreq);
                    } else {
                        posFreq.put(pos, posFreq.getOrDefault(pos, 0) + 1);
                    }
                }
            }
        }
//        System.out.println(freqMap);
        int posSumMax = 0;  // 词性合起来频次最高的
        int individualMax = 0;  // 单个词性最高的
        Result best = null;
        for (Map.Entry<String, Map<String, Integer>> desPosFreq : freqMap.entrySet()) {
            String engDes = desPosFreq.getKey();
            int posIndividualMax = 0;
            Result posIndividualBest = null;
            int posSum = 0;
            for (Map.Entry<String, Integer> posFreq : desPosFreq.getValue().entrySet()) {
                int freq = posFreq.getValue();
                posSum += freq;
                if (freq > posIndividualMax) {
                    posIndividualMax = freq;
                    posIndividualBest = new Result(engDes, posFreq.getKey(), results.matchLength);
                }
            }
            // 如果词性的频次最高，取它
            // 如果不是最高，取所有词性总和最高的词
            if (posIndividualMax > individualMax) {
                individualMax = posIndividualMax;
                best = posIndividualBest;

                if (posSum > posSumMax) {  // 同时也更新这个posSumMax
                    posSumMax = posSum;
                }
                continue;
            }

            if (posSum > posSumMax) {
                posSumMax = posSum;
                best = posIndividualBest;
            }
        }

        return best;
    }
}
