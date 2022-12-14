package trashsoftware.duckSonTranslator.wordPickers.wordPickerGegChs;

import trashsoftware.duckSonTranslator.dict.BigDict;
import trashsoftware.duckSonTranslator.dict.BigDictValue;
import trashsoftware.duckSonTranslator.wordPickers.ResultFromLatin;
import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;

import java.util.*;

public class NaiveChsPicker extends ChsCharPicker {
    public NaiveChsPicker(BigDict bigDict, PickerFactory factory) {
        super(bigDict, factory);
    }

    @Override
    protected ResultFromLatin translateOneWordInner(String engWord) {
        BigDictValue chs = bigDict.getByEng(engWord, false);
        if (chs == null) return ResultFromLatin.NOT_FOUND;
        String[] chsPos = pickBestChs(engWord, chs);
//        if (chsPos == null) return null;
        return new ResultFromLatin(chsPos[0], chsPos[1]);
    }

    private String[] pickBestChs(String engWord, BigDictValue dictValue) {
        Map<Character, ChsCharFreq> charFreq = new HashMap<>();  // 要保持顺序
        for (Map.Entry<String, Set<String>> posChs : dictValue.value.entrySet()) {
            for (String chs : posChs.getValue()) {
                int index = 0;
                for (char c : chs.toCharArray()) {
                    if (!CHS_EXCEPTIONS.contains(c)) {
                        ChsCharFreq ccf = charFreq.get(c);
                        if (ccf == null) {
                            Map<String, BigDictValue> allMatches = bigDict.getAllMatches(c);
                            ccf = new ChsCharFreq(c, index, allMatches.size(), posChs.getKey());
                            
                            for (var entry : allMatches.entrySet()) {
                                String chsWord = entry.getKey();
                                if (chsWord.length() == 1) {
                                    for (var posDes : entry.getValue().value.entrySet()) {
                                        String ep = posDes.getKey();
                                        for (String engDes : posDes.getValue()) {
                                            if (engDes.equals(engWord)) {
                                                ccf.exact = true;
                                                ccf.anyPos = ep;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            
                            charFreq.put(c, ccf);
                        }
                        ccf.freq++;
                    }
                    index++;
                }
            }
        }
        List<ChsCharFreq> chsCharFreqList = new ArrayList<>(charFreq.values());
        Collections.sort(chsCharFreqList);
        Collections.reverse(chsCharFreqList);
        ChsCharFreq result = chsCharFreqList.get(0);
        return new String[]{String.valueOf(result.chsChar), result.anyPos};
    }
    
    private static class ChsCharFreq implements Comparable<ChsCharFreq> {
        final char chsChar;
        final int firstOccurIndex;
        final int occurrenceInOtherWords;
        String anyPos;  // 翻译回中文时词性不太重要，留第一个就行
        boolean exact;  // 是不是有个中文字正好就是这个意思。如果有，顺便把词性也改了
        int freq;

        ChsCharFreq(char chsChar, int firstOccurIndex, int occurrenceInOtherWords, String firstPos) {
            this.chsChar = chsChar;
            this.firstOccurIndex = firstOccurIndex;
            this.occurrenceInOtherWords = occurrenceInOtherWords;
            this.anyPos = firstPos;
        }

        @Override
        public int compareTo(ChsCharFreq o) {
            if (this.exact && !o.exact) return 1;
            else if (!this.exact && o.exact) return -1;
            
            int freqCmp = Integer.compare(this.freq, o.freq);
            if (freqCmp != 0) return freqCmp;

            int otherOccCmp = Integer.compare(this.occurrenceInOtherWords, o.occurrenceInOtherWords);
            if (otherOccCmp != 0) return -otherOccCmp;

            int indexCmp = Integer.compare(this.firstOccurIndex, o.firstOccurIndex);
            if (indexCmp != 0) return -indexCmp;

            return Character.compare(this.chsChar, o.chsChar);
        }

        @Override
        public String toString() {
            return "ChsCharFreq{" + chsChar +
                    "@" + firstOccurIndex +
                    ", freq=" + freq +
                    ", other=" + occurrenceInOtherWords +
                    '}';
        }
    }
}
