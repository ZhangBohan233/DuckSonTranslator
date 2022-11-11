package trashsoftware.duckSonTranslator.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;

import java.util.*;

import static trashsoftware.duckSonTranslator.wordPickerChsGeg.CommonPrefixCharPicker.MIN_SUBSTRING_LENGTH;
import static trashsoftware.duckSonTranslator.wordPickerChsGeg.CommonPrefixCharPicker.commonSubstringLength;

public class CombinedCharPicker extends SingleCharPicker {
    protected CombinedCharPicker(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }

    @Override
    protected Result translateChar(char chs) {
        var allMatches = bigDict.getAllMatches(chs);
        Map<String, Candidate> candidateMap = new HashMap<>();  // eng: {pos: [含chs的词数, 不含的词数]}
        for (var chsWordDes : allMatches.entrySet()) {
//            var chsWord = chsWordDes.getKey();
            var engPosDesList = chsWordDes.getValue().value;
            for (var engPosDes : engPosDesList.entrySet()) {
                var pos = engPosDes.getKey();
                for (var des : engPosDes.getValue()) {
//                    System.out.println(pos + " " + des);
                    var chsValues = bigDict.getEngChsMap().get(des);
                    for (var posChs : chsValues.value.entrySet()) {
                        if (!posChs.getKey().equals(pos)) {
                            continue;  // 去重
                        }
//                        System.out.println(posChs.getKey() + " " + posChs.getValue());
                        Candidate candidate = candidateMap.computeIfAbsent(des, k -> new Candidate(des, chs));
                        for (String invChsDes : posChs.getValue()) {
//                            candidate.chsUniqueDes.add(invChsDes);
                            Set<String> desOfThisPos = candidate.posDes.computeIfAbsent(pos, k -> new HashSet<>());
                            desOfThisPos.add(invChsDes);
                            
                            if (invChsDes.indexOf(chs) != -1) {
                                if (invChsDes.length() == 1 && invChsDes.charAt(0) == chs) {
                                    candidate.exactMatch = true;
                                    candidate.bestPos = pos;
                                }
//                                candidate.matches.add(invChsDes);
                                Set<String> posMatches = candidate.posMatches.computeIfAbsent(pos, k -> new HashSet<>());
                                posMatches.add(invChsDes);
                            }
                        }
                    }
                }
            }
        }
        if (candidateMap.isEmpty()) return null;
        List<Candidate> candidateList = new ArrayList<>(candidateMap.values());
        for (Candidate candidate : candidateList) {
            candidate.findAllSuperStrings(candidateList);
        }
        Collections.sort(candidateList);
        Collections.reverse(candidateList);
//        System.out.println(chs);
//        System.out.println(candidateList);
        
        Candidate best = candidateList.get(0);
        
        return new Result(best.eng, best.bestPos, 1, best.resultPrecedence());
    }
    
    private static class Candidate implements Comparable<Candidate> {
        final String eng;
        final char chs;
        boolean exactMatch;
//        final Set<String> chsUniqueDes = new HashSet<>();  // 该词所有中文释义
//        final Set<String> matches = new HashSet<>();  // 含目标chs的中文释义
        final Map<String, Set<String>> posDes = new HashMap<>();  // 每个pos所有match集合
        final Map<String, Set<String>> posMatches = new HashMap<>();  // 每个pos的match集合
        private final Set<String> superStrings = new HashSet<>();  // 这个的父串集合
        
        private int minWordLen = Integer.MAX_VALUE;
        private int minOccurIndex = Integer.MAX_VALUE;
        
        private String bestPos;
        private double bestPosPurity;

        Candidate(String eng, char chs) {
            this.eng = eng;
            this.chs = chs;
        }

        void findAllSuperStrings(Collection<Candidate> allCandidates) {
            for (Candidate candidate : allCandidates) {
                if (!candidate.eng.equals(this.eng)) {
                    int cmp = commonSubstringLength(this.eng, candidate.eng);
                    if (cmp <= -MIN_SUBSTRING_LENGTH) {  // 太短的还是算了
                        superStrings.add(candidate.eng);
                    }
                }
            }
//            updatePosFreq();
            compute();
        }
        
        private void compute() {
//            int totalWordLen = 0;
//            for (String match : matches) {
//                totalWordLen += match.length();
//            }
//            avgWordLen = (double) matches.size() / totalWordLen;
            
            if (bestPos == null) {
                for (var entry : posMatches.entrySet()) {
                    Set<String> posAllDes = posDes.get(entry.getKey());
                    double posPurity = (double) entry.getValue().size() / posAllDes.size();
                    if (posPurity > bestPosPurity) {
                        bestPosPurity = posPurity;
                        bestPos = entry.getKey();
                    }
                }
            } else {
                bestPosPurity = 1.0;
            }
            if (bestPos == null) throw new RuntimeException();
            
            Set<String> bestPosDes = posMatches.get(bestPos);
            for (String s : bestPosDes) {
                int len = s.length();
                int index = s.indexOf(chs);
                if (index == -1) throw new RuntimeException();
                
                if (len < minWordLen) minWordLen = len;
                if (index < minOccurIndex) minOccurIndex = index;
            }
        }
        
        private double resultPrecedence() {
            return exactMatch ? (1.0 / eng.length() * 2) : (bestPosPurity / eng.length());
        }
        
        @Override
        public int compareTo(Candidate o) {
            if (this.exactMatch && !o.exactMatch) return 1;
            if (!this.exactMatch && o.exactMatch) return -1;
            
            if (this.superStrings.contains(o.eng)) return 1;
            if (o.superStrings.contains(this.eng)) return -1;
            
            int purityCmp = Double.compare(this.bestPosPurity, o.bestPosPurity);
            if (purityCmp != 0) return purityCmp;
            
            int avgOccurIndex = Double.compare(this.minOccurIndex, o.minOccurIndex);
            if (avgOccurIndex != 0) return -avgOccurIndex;

            int avgLenCmp = Double.compare(this.minWordLen, o.minWordLen);
            if (avgLenCmp != 0) return -avgLenCmp;
            
            return -Integer.compare(this.eng.length(), o.eng.length());
        }

        @Override
        public String toString() {
            return "Candidate{" + eng +
                    ": " + (exactMatch ? "exact, " : "") +
                    ", pos=" + posMatches +
                    ", superStrings=" + superStrings +
                    ", " + bestPos + ", " + bestPosPurity +
                    ", " + minOccurIndex + 
                    ", " + minWordLen +
                    '}';
        }
    }
}
