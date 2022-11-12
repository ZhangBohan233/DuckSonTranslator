package trashsoftware.duckSonTranslator.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;

import java.util.*;

import static trashsoftware.duckSonTranslator.wordPickerChsGeg.CommonPrefixCharPicker.MIN_SUBSTRING_LENGTH;
import static trashsoftware.duckSonTranslator.wordPickerChsGeg.CommonPrefixCharPicker.commonSubstringLength;

public class CombinedCharPicker extends SingleCharPicker {
    
    private static final List<String> POS_PRECEDENCE = List.of(
            "v", "pron", "n"
    );
    
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
                        String nearPos = posChs.getKey();
                        if (!posChs.getKey().equals(pos) && engPosDesList.containsKey(nearPos)) {
                            continue;  // 去重
                        }
//                        System.out.println(posChs.getKey() + " " + posChs.getValue());
                        Candidate candidate = candidateMap.computeIfAbsent(des, k -> new Candidate(des, chs));
                        for (String invChsDes : posChs.getValue()) {
//                            candidate.chsUniqueDes.add(invChsDes);
                            Set<String> desOfThisPos = candidate.posDes.computeIfAbsent(nearPos, k -> new HashSet<>());
                            desOfThisPos.add(invChsDes);

                            if (invChsDes.indexOf(chs) != -1) {
                                if (invChsDes.length() == 1 && invChsDes.charAt(0) == chs) {
                                    // is exact
                                    if (isBetterExactMatch(candidate, nearPos)) {
                                        candidate.exactMatch = true;
                                        candidate.bestPos = nearPos;
                                    }
                                }
//                                candidate.matches.add(invChsDes);
                                Set<String> posMatches = candidate.posMatches.computeIfAbsent(nearPos, k -> new HashSet<>());
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

    /**
     * Precondition: newExactPos是exact
     */
    private static boolean isBetterExactMatch(Candidate candidate, String newExactPos) {
        if (candidate.exactMatch && candidate.bestPos != null) {
            int curIndex = POS_PRECEDENCE.indexOf(candidate.bestPos);
            if (curIndex == -1) curIndex = Integer.MAX_VALUE;

            int newIndex = POS_PRECEDENCE.indexOf(newExactPos);
            if (newIndex == -1) newIndex = Integer.MAX_VALUE;
            
            return newIndex < curIndex;
        } else {
            return true;
        }
    }

    private static class Candidate implements Comparable<Candidate> {
        final String eng;
        final char chs;
        final Map<String, Set<String>> posDes = new HashMap<>();  // 每个pos所有match集合
        final Map<String, Set<String>> posMatches = new HashMap<>();  // 每个pos的match集合
        private final Set<String> superStrings = new HashSet<>();  // 这个的父串集合
        boolean exactMatch;
        private int minWordLen = Integer.MAX_VALUE;
        private int minOccurIndex = Integer.MAX_VALUE;

        private String bestPos;
        private double bestPosPurity;
        private double totalPurity;

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
            compute();
        }

        private void compute() {
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

            int allPosTotal = 0;
            int allPosMatch = 0;
            for (var entry : posMatches.entrySet()) {
//                if (!entry.getKey().equals(bestPos)) {
                    Set<String> posAllDes = posDes.get(entry.getKey());
                    allPosTotal += posAllDes.size();
                    allPosMatch += entry.getValue().size();
//                }
            }
            totalPurity = (double) allPosMatch / allPosTotal;
//            if (Double.isNaN(otherPosPurity)) otherPosPurity = 0.99;

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
            
            int otherCmp = Double.compare(this.totalPurity, o.totalPurity);
            if (otherCmp != 0) return otherCmp;

            return -Integer.compare(this.eng.length(), o.eng.length());
        }

        @Override
        public String toString() {
            return "Candidate{" + eng +
                    ": " + (exactMatch ? "exact, " : "") +
                    ", posMatch=" + posMatches +
                    ", allPos=" + posDes +
                    ", superStrings=" + superStrings +
                    ", " + bestPos + ", " + bestPosPurity +
                    ", " + minOccurIndex +
                    ", " + minWordLen +
                    ", " + totalPurity +
                    '}';
        }
    }
}
