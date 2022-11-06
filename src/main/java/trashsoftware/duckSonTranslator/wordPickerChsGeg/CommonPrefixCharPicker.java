package trashsoftware.duckSonTranslator.wordPickerChsGeg;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.Util;

import java.util.*;

public class CommonPrefixCharPicker extends SingleCharPicker {
    
    protected CommonPrefixCharPicker(BigDict bigDict) {
        super(bigDict);
    }

    @Override
    protected Result translateChar(char chs) {
        var matches = bigDict.getAllMatches(chs);
        Map<String, Candidate> engCandidates = new HashMap<>();
        for (var entry : matches.entrySet()) {
            for (var posDesList : entry.getValue().value.entrySet()) {
                String pos = posDesList.getKey();
                for (String eng : posDesList.getValue()) {
                    Candidate get = engCandidates.get(eng);
                    if (get == null) {
                        engCandidates.put(eng, new Candidate(eng, pos, entry.getKey()));
                    } else {
                        List<String> meanOfPos = get.posMeanings.computeIfAbsent(pos, k -> new ArrayList<>());
                        meanOfPos.add(entry.getKey());
                    }
                }
            }
        }
//        System.out.println(engCandidates);
        if (engCandidates.isEmpty()) return null;
        List<Candidate> candidateList = new ArrayList<>(engCandidates.values());
        Collections.sort(candidateList);
        Collections.reverse(candidateList);
//        System.out.println(chs + " " + candidateList);
        Candidate best = candidateList.get(0);
        return new Result(best.eng, best.bestPartOfSpeech, 1);
    }

    /**
     * 返回公共子串的长度。
     * 
     * 如果b是a的子串，或b更像a的子串，结果为正数
     */
    static int commonSubstringLength(String a, String b) {
        int sign = 1;
        if (b.length() > a.length()) {
            String tmp = b;
            b = a;
            a = tmp;
            sign = -1;
        }
        
        int aLen = a.length();
        int bLen = b.length();
        
        int maxLen = 0;
        for (int aOff = 0; aOff < aLen; aOff++) {
            for (int bOff = 0; bOff < bLen; bOff++) {
                int remLen = Math.min(aLen - aOff, bLen - bOff);
                for (int len = 1; len <= remLen; len++) {
                    if (strEquals(a, aOff, b, bOff, len)) {
                        if (len > maxLen) {
                            maxLen = len;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        
        return maxLen * sign;
    }
    
    private static boolean strEquals(String a, int aOffset, String b, int bOffset, int length) {
        for (int i = 0; i < length; i++) {
            if (a.charAt(aOffset + i) != b.charAt(bOffset + i)) return false;
        }
        return true;
    }
    
    private static class Candidate implements Comparable<Candidate> {
        final String eng;
        final Map<String, List<String>> posMeanings = new HashMap<>();
        private String bestPartOfSpeech;
        private int totalPosFreq = -1;
        
        Candidate(String eng, String firstPos, String firstDes) {
            this.eng = eng;
            this.posMeanings.put(firstPos, new ArrayList<>(List.of(firstDes)));
//            this.chsFull = chsFull;
        }
        
        private void updatePosFreq() {
            if (totalPosFreq == -1 || bestPartOfSpeech == null) {
                int most = 0;
                totalPosFreq = Util.countUniqueMeanings(posMeanings);
                for (var entry : posMeanings.entrySet()) {
                    if (entry.getValue().size() > most) {
                        most = entry.getValue().size();
                        bestPartOfSpeech = entry.getKey();
                    }
                }
            }
        }

        @Override
        public int compareTo(Candidate o) {
            this.updatePosFreq();
            o.updatePosFreq();
            
            int substringVal = commonSubstringLength(this.eng, o.eng);
            if (Math.abs(substringVal) >= 3) {  // 太短的还是算了
                if (substringVal > 0) return -1;
                else if (substringVal < 0) return 1;
            }
            
            int freqCmp = Integer.compare(this.totalPosFreq, o.totalPosFreq);
            if (freqCmp != 0) return freqCmp;
            
            return -Integer.compare(this.eng.length(), o.eng.length());
        }

        @Override
        public String toString() {
            return "Candidate{" + eng + " " + totalPosFreq + ": " + posMeanings + '}';
        }
    }
}
