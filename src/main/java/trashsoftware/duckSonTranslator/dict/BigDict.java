package trashsoftware.duckSonTranslator.dict;

import trashsoftware.duckSonTranslator.trees.Trie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class BigDict {

    public static final Set<Character> CHS_EXCEPTIONS = Set.of(
            '的', '（', '）', '(', ')'
    );
    protected final Trie<BigDictValue> chsEngTrie = new Trie<>();
    protected final Map<String, BigDictValue> engChsMap = new HashMap<>();
    protected final Map<String, BigDictValue> chsEngMap = new HashMap<>();

    public BigDict() throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(
                        DictMaker.class.getResourceAsStream("eng_chs.txt"))))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.strip().startsWith("#")) continue;

                String[] split = line.split(" {3}");
                List<String> stripped = Arrays.stream(split)
                        .map(String::strip)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList());
                if (stripped.size() >= 2) {
                    String eng = stripped.get(0);
                    List<String> desPart = new ArrayList<>();
                    for (String spl : stripped.subList(1, stripped.size())) {
                        List<String> spl2 = Arrays.stream(spl.split(" "))
                                .map(String::strip)
                                .collect(Collectors.toList());
                        desPart.addAll(spl2);
                    }

                    for (String posDesPart : desPart) {
                        Map<String, List<String>> posDes = new TreeMap<>();
                        String[] poses = posDesPart.split("\\.,");
                        if (poses.length == 1) {
                            poses = poses[0].split("\\.");
                            for (int i = 0; i < poses.length; i += 2) {
                                posDes.put(
                                        poses[i],
                                        Arrays.stream(poses[i + 1].split("，"))
                                                .map(String::strip)
                                                .collect(Collectors.toList())
                                );
                            }
                        } else {
                            String[] last = poses[poses.length - 1].split("\\.");
                            List<String> posesList = new ArrayList<>(List.of(poses).subList(0, poses.length - 1));
                            posesList.addAll(Arrays.asList(last).subList(0, last.length - 1));

//                            posesList.addAll(Arrays.stream(last).toList().subList(0, last.length - 1));
                            List<String> des = Arrays.stream(last[last.length - 1]
                                            .split("，"))
                                    .map(String::strip)
                                    .collect(Collectors.toList());
                            for (String pos : posesList) {
                                posDes.put(pos, des);
                            }
                        }

                        BigDictValue sameWordDiffPos = engChsMap.get(eng);
                        if (sameWordDiffPos == null) {
                            engChsMap.put(eng, new BigDictValue(posDes));
                        } else {
                            sameWordDiffPos.value.putAll(posDes);
                        }
//                        System.out.println(eng + " " + engChsMap.get(eng));

                        for (Map.Entry<String, List<String>> pd : posDes.entrySet()) {
                            for (String des : pd.getValue()) {
                                BigDictValue val = chsEngMap.get(des);
                                if (val == null) {
                                    Map<String, List<String>> engPosDes = new TreeMap<>();
                                    List<String> engDes = new ArrayList<>();
                                    engDes.add(eng);
                                    engPosDes.put(pd.getKey(), engDes);
                                    val = new BigDictValue(engPosDes);
                                    chsEngMap.put(des, val);
                                } else {
                                    List<String> engDes = val.value.get(pd.getKey());
                                    if (engDes == null) {
                                        engDes = new ArrayList<>();
                                        engDes.add(eng);
                                        val.value.put(pd.getKey(), engDes);
                                    } else {
                                        engDes.add(eng);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (Map.Entry<String, BigDictValue> entry : chsEngMap.entrySet()) {
                chsEngTrie.insert(entry.getKey(), entry.getValue());
            }
//            System.out.println(engChsMap);
//            System.out.println(chsEngTrie.get("经济学人"));
        }
    }

    public Trie<BigDictValue> getChsEngTrie() {
        return chsEngTrie;
    }

    public Map<String, BigDictValue> getEngChsMap() {
        return engChsMap;
    }

    public Map<String, BigDictValue> getChsEngMap() {
        return chsEngMap;
    }

    /**
     * 返回每个词性的吻合度
     */
    private Purity posDesOfMaxPurity(String chs, Trie.Match<BigDictValue> match) {
        String purestPos = null;  // 最好的词性
        String purestDes = null;  // 最好的英文
        double purest = 0.0;
        for (Map.Entry<String, List<String>> posDes : match.value.value.entrySet()) {
            String pos = posDes.getKey();
            int posTotal = 0;
            int posMatchLen = 0;
            double wordPurest = 0.0;
            String posPurestDes = null;  // 这个词性最好的英文
            for (String des : posDes.getValue()) {
                BigDictValue reverseTrans = engChsMap.get(des);

                int wordTotal = 0;
                int wordMatchLen = 0;
                List<String> engChsSamePos = reverseTrans.value.get(pos);
                if (engChsSamePos != null) {
                    for (String chsDes : engChsSamePos) {
                        wordTotal += chsDes.length();
                        if (chsDes.contains(chs)) {
                            wordMatchLen += chs.length();
                        }
                    }
                }
                double wordPurity = (double) wordMatchLen / wordTotal;
                if (wordPurity > wordPurest) {
                    wordPurest = wordPurity;
                    posPurestDes = des;
                }

                posTotal += wordTotal;
                posMatchLen += wordMatchLen;
            }
            double posPurity = (double) posMatchLen / posTotal;
            if (posPurity > purest) {
                purest = posPurity;
                purestPos = pos;
                purestDes = posPurestDes;
            }

//            System.out.println(pos + " " + posPurity);
        }
//        System.out.println(purest + " " + purestPos + " " + purestDes);
        return new Purity(purestPos, purestDes, purest);
    }

    public Map<String, BigDictValue> getAllMatches(char chs) {
        Map<String, BigDictValue> allMatches = new HashMap<>();
        for (Map.Entry<String, BigDictValue> entry : chsEngMap.entrySet()) {
            if (entry.getKey().indexOf(chs) != -1) {
                allMatches.put(entry.getKey(), entry.getValue());
            }
        }
        return allMatches;
    }

    private Purity2 purityOfWord(char chsChar, String pos, String eng, List<String> chsDesOfPos) {
//        System.out.println(chsChar + " " + eng + chsDesOfPos);
        int matched = 0;
        for (String cheDes : chsDesOfPos) {
            if (cheDes.indexOf(chsChar) != -1) {
                matched++;
            }
        }
        double purity = (double) matched / chsDesOfPos.size();
        return new Purity2(pos, eng, chsDesOfPos, purity);
    }

    

    public ChsResult translateEngToChs(String engWord) {
        BigDictValue chs = engChsMap.get(engWord);
        if (chs == null) return null;
        String[] chsPos = pickBestChs(chs);
        if (chsPos == null) return null;
        return new ChsResult(chsPos[0], chsPos[1]);
    }

//    private int occurrenceInOtherWords(char chsChar) {
//        
//    }

    private String[] pickBestChs(BigDictValue dictValue) {
        Map<Character, ChsCharFreq> charFreq = new HashMap<>();  // 要保持顺序
        for (Map.Entry<String, List<String>> posChs : dictValue.value.entrySet()) {
            for (String chs : posChs.getValue()) {
                int index = 0;
                for (char c : chs.toCharArray()) {
                    if (!CHS_EXCEPTIONS.contains(c)) {
                        ChsCharFreq ccf = charFreq.get(c);
                        if (ccf == null) {
                            Map<String, BigDictValue> allMatches = getAllMatches(c);
                            ccf = new ChsCharFreq(c, index, allMatches.size(), posChs.getKey());
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



    public static class ChsResult {
        public final String translated;
        public final String partOfSpeech;

        ChsResult(String translated, String partOfSpeech) {
            this.translated = translated;
            this.partOfSpeech = partOfSpeech;
        }

        @Override
        public String toString() {
            return String.format("(%s)%s", partOfSpeech, translated);
        }
    }

    private static class Purity implements Comparable<Purity> {
        final String pos;
        final String des;
        final double purity;

        Purity(String pos, String des, double purity) {
            this.pos = pos;
            this.des = des;
            this.purity = purity;
        }

        @Override
        public String toString() {
            return String.format("%s, %s, %f", pos, des, purity);
        }

        boolean betterThan(Purity o) {
            return compareTo(o) > 0;
        }

        @Override
        public int compareTo(Purity o) {
            if (this.purity == o.purity) {
                return -Integer.compare(this.des.length(), o.des.length());
            } else {
                return Double.compare(this.purity, o.purity);
            }
        }
    }

    private static class Purity2 extends Purity {
        final List<String> chsWords;

        Purity2(String pos, String des, List<String> chsWords, double purity) {
            super(pos, des, purity);

            this.chsWords = chsWords;
        }

        @Override
        public String toString() {
            return super.toString() + ", " + chsWords;
        }
    }



    private static class ChsCharFreq implements Comparable<ChsCharFreq> {
        final char chsChar;
        final int firstOccurIndex;
        final int occurrenceInOtherWords;
        final String anyPos;  // 翻译回中文时词性不太重要，留第一个就行
        int freq;

        ChsCharFreq(char chsChar, int firstOccurIndex, int occurrenceInOtherWords, String firstPos) {
            this.chsChar = chsChar;
            this.firstOccurIndex = firstOccurIndex;
            this.occurrenceInOtherWords = occurrenceInOtherWords;
            this.anyPos = firstPos;
        }

        @Override
        public int compareTo(ChsCharFreq o) {
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
