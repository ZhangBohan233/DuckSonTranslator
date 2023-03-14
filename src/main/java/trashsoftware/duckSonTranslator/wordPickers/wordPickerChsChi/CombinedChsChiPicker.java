package trashsoftware.duckSonTranslator.wordPickers.wordPickerChsChi;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.BigDictValue;
import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg.CombinedCharPicker;
import trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg.ResultFromChs;

import java.util.*;

public class CombinedChsChiPicker extends ChsChiWordPicker {

    public CombinedChsChiPicker(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }

    @Override
    public ResultFromChs translate(String sentence) {

        BigDict.WordMatch wordMatch = bigDict.findPrefixMatchesByChs(sentence, true, false);
        if (wordMatch == null || wordMatch.length == 0) return null;

        // 这个不是null就必定不是empty
//        System.out.println(sentence.substring(0, wordMatch.length) + wordMatch.length + " " + wordMatch.matches);

        String chs = sentence.substring(0, wordMatch.length);

        // 第一步: 找到最纯的中文
        SortedMap<Double, Map.Entry<String, BigDictValue>> chsPurityMap = new TreeMap<>();
        for (var chsWordAndDes : wordMatch.matches.entrySet()) {
            String chsWord = chsWordAndDes.getKey();
            double lengthPercent = (double) wordMatch.length / chsWord.length();
            chsPurityMap.put(lengthPercent, chsWordAndDes);
        }
        double maxPurity = chsPurityMap.lastKey();
        Map.Entry<String, BigDictValue> maxPurityEntry = chsPurityMap.get(maxPurity);
//        System.out.println(wordMatch.length + " " + maxPurityEntry);

        // 第二步: 通过英文反向查中文，找到最纯的英文
        Map<String, Candidate> engAndCandidates = new HashMap<>();
        for (var engPosDes : maxPurityEntry.getValue().value.entrySet()) {
//            String pos = engPosDes.getKey();
            for (String eng : engPosDes.getValue()) {
                BigDictValue inverseChsValue = bigDict.getEngChsHugeMap().get(eng);
//                System.out.println(inverseChsValue);
                for (var chsPosDes : inverseChsValue.value.entrySet()) {
                    String chsPos = chsPosDes.getKey();
                    Candidate candidate = engAndCandidates.get(eng);
                    if (candidate == null) {
                        candidate = new Candidate(chs, eng, maxPurity == 1.0);
                        engAndCandidates.put(eng, candidate);
                    }
                    Set<String> allOfThisPos = candidate.allPosDes.get(chsPos);
                    Set<String> matchOfThisPos = candidate.matchedPosDes.get(chsPos);
                    if (allOfThisPos == null) {
                        allOfThisPos = new HashSet<>();
                        matchOfThisPos = new HashSet<>();
                        candidate.allPosDes.put(chsPos, allOfThisPos);
                        candidate.matchedPosDes.put(chsPos, matchOfThisPos);
                    }
                    allOfThisPos.addAll(chsPosDes.getValue());
                    for (String chsDes : chsPosDes.getValue()) {
                        if (chsDes.contains(chs)) {
                            matchOfThisPos.add(chsDes);
                        }
                    }
                }
            }
        }
        List<Candidate> candidateList = new ArrayList<>(engAndCandidates.values());
        for (Candidate candidate : candidateList) {
            candidate.compute();
        }
        Collections.sort(candidateList);
        Collections.reverse(candidateList);

        // 必不empty
        Candidate best = candidateList.get(0);

//        System.out.println(candidateList);

        return new ResultFromChs(best.eng, best.bestPos, wordMatch.length);
    }

    private static class Candidate implements Comparable<Candidate> {
        final String chs;
        final String eng;
        boolean exactMatch;
        Map<String, Set<String>> allPosDes = new HashMap<>();
        Map<String, Set<String>> matchedPosDes = new HashMap<>();

        private String bestPos;
        private double bestPosPurity = -1;
        
        private int totalCount;
        private int totalMatchCount;
        private double totalPurity;

        private Candidate(String chs, String eng, boolean exactMatch) {
            this.chs = chs;
            this.eng = eng;
            this.exactMatch = exactMatch;
        }

        private void compute() {
            
            for (var posMatch : matchedPosDes.entrySet()) {
                String pos = posMatch.getKey();
                int posTotalCount = allPosDes.get(pos).size();
                totalCount += posTotalCount;
                int posMatchCount = 0;
                for (String chsDes : posMatch.getValue()) {
                    if (chsDes.contains(chs)) {
                        posMatchCount++;
                        totalMatchCount++;
                    }
                }
                double posPurity = (double) posMatchCount / posTotalCount;
                if (posPurity > bestPosPurity) {
                    bestPos = pos;
                    bestPosPurity = posPurity;
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
            if (this.exactMatch && !o.exactMatch) return 1;
            if (!this.exactMatch && o.exactMatch) return -1;
            
            int bestPurityCmp = Double.compare(this.bestPosPurity, o.bestPosPurity);
            if (bestPurityCmp != 0) return bestPurityCmp;

            int totalPurityCmp = Double.compare(this.totalPurity, o.totalPurity);
            if (totalPurityCmp != 0) return totalPurityCmp;
            
            return -Integer.compare(this.eng.length(), o.eng.length());
        }

        @Override
        public String toString() {
            return "Candidate{" +
                    "exactMatch=" + exactMatch +
                    ", chs='" + chs + '\'' +
                    ", eng='" + eng + '\'' +
                    ", allPosDes=" + allPosDes +
                    ", matchedPosDes=" + matchedPosDes +
                    '}';
        }
    }
}
