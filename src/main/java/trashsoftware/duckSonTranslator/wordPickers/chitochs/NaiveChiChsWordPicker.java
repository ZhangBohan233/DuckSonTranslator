package trashsoftware.duckSonTranslator.wordPickers.chitochs;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.BigDictValue;
import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;
import trashsoftware.duckSonTranslator.wordPickers.ResultFromLatin;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg.CombinedCharPicker;

import java.util.*;

public class NaiveChiChsWordPicker extends ChiChsWordPicker {
    
    public NaiveChiChsWordPicker(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }

    @Override
    public ResultFromLatin translate(String original) {
        BigDictValue bdv = bigDict.getByEng(original, true);
        if (bdv == null) return ResultFromLatin.NOT_FOUND;

        Map<String, Candidate> candidateMap = new HashMap<>();
        for (var entry : bdv.value.entrySet()) {
//            String chsPos = entry.getKey();
            for (String chsWord : entry.getValue()) {
                BigDictValue reverseEng = bigDict.getByChs(chsWord, true);
                if (reverseEng == null) continue;
//                System.out.println(original + chsWord + reverseEng);
                Candidate candidate = candidateMap.computeIfAbsent(chsWord, k -> new Candidate(original, chsWord));
                for (var engEntry : reverseEng.value.entrySet()) {
                    String revEngPos = engEntry.getKey();
                    for (String revEng : engEntry.getValue()) {
                        Set<String> posAll = candidate.posAllMap.computeIfAbsent(revEngPos, k -> new HashSet<>());
                        posAll.add(revEng);
                        if (revEng.equals(original)) {
                            Set<String> posMatch = candidate.posMatchMap.computeIfAbsent(revEngPos, k -> new HashSet<>());
                            posMatch.add(revEng);
                        }
                    }
                }
            }
        }
        if (candidateMap.isEmpty()) return ResultFromLatin.NOT_FOUND;
        
        List<Candidate> candidates = new ArrayList<>(candidateMap.values());
        for (Candidate candidate : candidates) {
            candidate.compute();
        }
        Collections.sort(candidates);
//        System.out.println(candidates);
        Candidate best = candidates.get(0);
        return new ResultFromLatin(best.chsWord, best.bestPos);
    }
    
    private static class Candidate implements Comparable<Candidate> {
        final String engWord;
        final String chsWord;
        String bestPos;
        
        int totalCount;
        int totalMatchCount;
        double totalPurity;
        double bestPosPurity = -1;
        
        private final Map<String, Set<String>> posAllMap = new HashMap<>();
        private final Map<String, Set<String>> posMatchMap = new HashMap<>();
        
        Candidate(String engWord, String chsWord) {
            this.engWord = engWord;
            this.chsWord = chsWord;
        }
        
        private void compute() {
            for (var entry : posMatchMap.entrySet()) {
                String pos = entry.getKey();
                Set<String> posAll = posAllMap.get(pos);
                totalMatchCount += entry.getValue().size();
                totalCount += posAll.size();
                
                double posPurity = (double) entry.getValue().size() / posAll.size();
                if (posPurity > bestPosPurity) {
                    bestPosPurity = posPurity;
                    bestPos = pos;
                } else if (posPurity == bestPosPurity) {
                    // 这里bestPos已经不会是null了
                    int thisPrecedence = CombinedCharPicker.POS_PRECEDENCE.indexOf(pos);
                    int curBestPrecedence = CombinedCharPicker.POS_PRECEDENCE.indexOf(bestPos);
                    if (thisPrecedence != -1) {  // 这个是存在的
                        if (curBestPrecedence == -1) {
                            bestPos = pos;
                        } else if (thisPrecedence < curBestPrecedence) {
                            // 比谁好
                            bestPos = pos;
                        }
                    }
                    // 没有else, 其他情况不替换
                }
            }
            totalPurity = (double) totalMatchCount / totalCount;
        }
        
        @Override
        public int compareTo(Candidate o) {
            // 好的return -1
            int bestPurityCmp = Double.compare(this.bestPosPurity, o.bestPosPurity);
            if (bestPurityCmp != 0) return -bestPurityCmp;

            int totalPurityCmp = Double.compare(this.totalPurity, o.totalPurity);
            if (totalPurityCmp != 0) return -totalPurityCmp;
            
            return Integer.compare(this.chsWord.length(), o.chsWord.length());
        }

        @Override
        public String toString() {
            return "Candidate{" +
                    "engWord='" + engWord + '\'' +
                    ", chsWord='" + chsWord + '\'' +
                    ", bestPos='" + bestPos + '\'' +
                    ", posAllMap=" + posAllMap +
                    ", posMatchMap=" + posMatchMap +
                    '}';
        }
    }
}
