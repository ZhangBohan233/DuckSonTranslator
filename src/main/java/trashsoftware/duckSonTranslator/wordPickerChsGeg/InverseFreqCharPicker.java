package trashsoftware.duckSonTranslator.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.BigDictValue;

import java.util.*;

public class InverseFreqCharPicker extends SingleCharPicker {
    protected InverseFreqCharPicker(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }

    private static void updateCandidatesMap(Map<String, SingleChsCharCandidate> subMap,
                                            Map<String, SingleChsCharCandidate> fullMap) {
        for (Map.Entry<String, SingleChsCharCandidate> entry : subMap.entrySet()) {
            SingleChsCharCandidate can = fullMap.get(entry.getKey());
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
    protected Result translateChar(char chs) {
        Map<String, BigDictValue> allMatches = bigDict.getAllMatches(chs);
//        SortedMap<Double, List<Purity2>> purityMap = new TreeMap<>();
        Map<String, SingleChsCharCandidate> candidates = new HashMap<>();
        for (Map.Entry<String, BigDictValue> entry : allMatches.entrySet()) {
//            String chsWord = entry.getKey();
//            if (chsWord.length() == 1 && chsWord.charAt(0) == chs) {
//                System.out.println("Exact match!");
//                return new Result()  todo: try this
//            }
            Map<String, List<String>> engPosDes = entry.getValue().value;

            Map<String, SingleChsCharCandidate> thisCandidate = createCandidate(chs, engPosDes);
            updateCandidatesMap(thisCandidate, candidates);
        }
        if (candidates.isEmpty()) return null;
//        System.out.println(candidates);
        List<SingleChsCharCandidate> candidateList = new ArrayList<>(candidates.values());
        Collections.sort(candidateList);
        Collections.reverse(candidateList);
//        System.out.println(candidateList);
        SingleChsCharCandidate candidate = candidateList.get(0);
        return new Result(candidate.engWord, candidate.bestPartOfSpeech(), 1);
    }

    private Map<String, SingleChsCharCandidate> createCandidate(char chsChar,
                                                                        Map<String, List<String>> engPosDes) {
        Map<String, SingleChsCharCandidate> engAndCandidates = new HashMap<>();
        for (Map.Entry<String, List<String>> posDes : engPosDes.entrySet()) {
            String pos = posDes.getKey();

            for (String eng : posDes.getValue()) {
                BigDictValue reverse = bigDict.getEngChsMap().get(eng);
                SingleChsCharCandidate can = engAndCandidates.computeIfAbsent(eng, SingleChsCharCandidate::new);

                for (Map.Entry<String, List<String>> chsPosDes : reverse.value.entrySet()) {
//                    System.out.println(chsPosDes);
                    String engPos = chsPosDes.getKey();
                    if (!pos.equals(engPos)) continue;

                    List<String> chsDes = chsPosDes.getValue();

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

        return engAndCandidates;
    }

    private static class SingleChsCharCandidate implements Comparable<SingleChsCharCandidate> {

        final Map<String, int[]> posPurity = new HashMap<>();
        final String engWord;
        final List<Integer> matchIndices = new ArrayList<>();
        private int total = -1;  // 临时值
        private int totalWords = -1;
        private int totalWordLengths = -1;
        private int totalMatches = -1;
        private double avgMatchIndex = -1.0;

        SingleChsCharCandidate(String engWord) {
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
        public int compareTo(SingleChsCharCandidate o) {
            if (this.engWord.startsWith(o.engWord)) {
                return -1;
            }

            this.updateComparisons();
            o.updateComparisons();

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
