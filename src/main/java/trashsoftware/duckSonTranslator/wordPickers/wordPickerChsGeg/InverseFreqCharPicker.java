package trashsoftware.duckSonTranslator.wordPickers.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.BigDictValue;
import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;

import java.util.*;

public class InverseFreqCharPicker extends SingleCharPicker {
    public InverseFreqCharPicker(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }

    private static void updateCandidatesMap(Map<String, Candidate> subMap,
                                            Map<String, Candidate> fullMap) {
        for (Map.Entry<String, Candidate> entry : subMap.entrySet()) {
            Candidate can = fullMap.get(entry.getKey());
            if (can == null) {
                fullMap.put(entry.getKey(), entry.getValue());
            } else {
                can.updatePosPurity(entry.getValue().posPurity);
            }
        }
    }

    private static int countChar(char c, String s) {
        int count = 0;
        for (char cc : s.toCharArray()) {
            if (c == cc) count++;
        }
        return count;
    }

    @Override
    protected ResultFromChs translateChar(char chs) {
        Map<String, BigDictValue> allMatches = bigDict.getAllMatches(chs);
        if (allMatches.isEmpty()) return ResultFromChs.NOT_FOUND;
//        SortedMap<Double, List<Purity2>> purityMap = new TreeMap<>();
        Map<String, Candidate> candidates = new HashMap<>();
        for (Map.Entry<String, BigDictValue> entry : allMatches.entrySet()) {
            Map<String, Set<String>> engPosDes = entry.getValue().value;

            Map<String, Candidate> thisCandidate = createCandidate(chs, engPosDes);
            updateCandidatesMap(thisCandidate, candidates);
        }
        if (candidates.isEmpty()) return ResultFromChs.NOT_FOUND;
//        System.out.println(candidates);
        List<Candidate> candidateList = new ArrayList<>(candidates.values());

        for (Candidate scc : candidateList) {
            scc.updateComparisons();
        }
//        System.out.println(candidateList);
        Collections.sort(candidateList);
        Collections.reverse(candidateList);
//        System.out.println(candidateList);
        Candidate candidate = candidateList.get(0);
        return new ResultFromChs(candidate.engWord, candidate.bestPartOfSpeech(), 1);
    }

    private Map<String, Candidate> createCandidate(char chsChar,
                                                   Map<String, Set<String>> engPosDes) {
        Map<String, Candidate> engAndCandidates = new HashMap<>();
        for (Map.Entry<String, Set<String>> posDes : engPosDes.entrySet()) {
            String pos = posDes.getKey();

            for (String eng : posDes.getValue()) {
                BigDictValue reverse = bigDict.getEngChsMap().get(eng);
                Candidate can = engAndCandidates.computeIfAbsent(eng, Candidate::new);

                for (Map.Entry<String, Set<String>> chsPosDes : reverse.value.entrySet()) {
//                    System.out.println(chsPosDes);
                    String engPos = chsPosDes.getKey();
                    if (!pos.equals(engPos)) continue;

                    Set<String> chsDes = chsPosDes.getValue();

                    int posMatch = 0;
                    int totalMatch = 0;
                    for (String chs : chsDes) {
                        int charIndex = chs.indexOf(chsChar);
                        if (charIndex != -1) {
                            posMatch++;
                            can.matchIndices.add(charIndex);
                        }
//                        posMatch += countChar(chsChar, chs);
                        totalMatch += chs.length();
                    }
//                    double posPurity = (double) posMatch / chsPosDes.getValue().size();
                    int[] matchTotal = can.posPurity.get(pos);
                    if (matchTotal != null) {
                        matchTotal[0] += posMatch;
                        matchTotal[1] += chsDes.size();
                        matchTotal[2] += totalMatch;
                    } else {
                        can.posPurity.put(pos, new int[]{posMatch, chsDes.size(), totalMatch});
                    }
                }
            }
        }
//        System.out.println(engAndCandidates);

        return engAndCandidates;
    }

    private static class Candidate implements Comparable<Candidate> {

        final Map<String, int[]> posPurity = new HashMap<>();
        final String engWord;
        final List<Integer> matchIndices = new ArrayList<>();
        private int total = -1;  // 临时值
        private int totalWords = -1;
        private int totalWordLengths = -1;
        private int totalMatches = -1;
        private double avgMatchIndex = -1.0;

        Candidate(String engWord) {
            this.engWord = engWord;
        }

        void updatePosPurity(Map<String, int[]> otherPp) {
            for (Map.Entry<String, int[]> entry : otherPp.entrySet()) {
                int[] pp = posPurity.get(entry.getKey());
                if (pp == null) {
                    posPurity.put(entry.getKey(), entry.getValue());
                } else {
                    pp[0] += entry.getValue()[0];
                    pp[1] += entry.getValue()[1];
                    pp[2] += entry.getValue()[2];
                }
            }
        }

        String bestPartOfSpeech() {
            int max = 0;
            String maxVal = null;
            for (Map.Entry<String, int[]> entry : posPurity.entrySet()) {
                if (entry.getValue()[0] > max) {
                    max = entry.getValue()[0];
                    maxVal = entry.getKey();
                }
            }
            if (maxVal == null) throw new RuntimeException(this.toString());
            return maxVal;
        }

        private void updateComparisons() {
            if (total == -1) {
                totalMatches = 0;
                total = 0;
                totalWordLengths = 0;
                totalWords = 0;
                for (int[] val : posPurity.values()) {
                    totalWords++;
                    totalMatches += val[0];
                    total += val[1];
                    totalWordLengths += val[2];
                }
                if (matchIndices.size() == 0) {
                    avgMatchIndex = 0.0;
                } else {
                    avgMatchIndex = (double) matchIndices.stream().reduce(Integer::sum).get() / matchIndices.size();
                }
            }
        }

        @Override
        public int compareTo(Candidate o) {
            if (this.engWord.startsWith(o.engWord)) {
                return -1;
            }

            double purity = (double) totalMatches / total;
            double oPurity = (double) o.totalMatches / o.total;

            if (purity < oPurity) return -1;
            else if (purity > oPurity) return 1;

            int indexCmp = Double.compare(this.avgMatchIndex, o.avgMatchIndex);
            if (indexCmp != 0) return -indexCmp;  // 我们希望正确的字出现在前面

            if (totalMatches < o.totalMatches) return -1;
            else if (totalMatches > o.totalMatches) return 1;

            double avgWordLen = (double) totalWordLengths / totalWords;
            double oAvgWordLen = (double) o.totalWordLengths / o.totalWords;
            int avgLenCmp = Double.compare(avgWordLen, oAvgWordLen);
            if (avgLenCmp != 0) return -avgLenCmp;

            return -Integer.compare(engWord.length(), o.engWord.length());
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, int[]> entry : posPurity.entrySet()) {
                builder.append(entry.getKey())
                        .append("=")
                        .append(Arrays.toString(entry.getValue()))
                        .append(", ");
            }

            return "(" + engWord + ": " + builder + ")";
        }
    }
}
