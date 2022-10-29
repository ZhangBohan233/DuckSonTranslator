package trashsoftware.duckSonTranslator.dict;

import trashsoftware.duckSonTranslator.trees.Trie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class BigDict {

    protected final Trie<BigDictValue> chsEngTrie = new Trie<>();
    protected final Map<String, BigDictValue> engChsMap = new TreeMap<>();

    public BigDict() throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(
                        DictMaker.class.getResourceAsStream("eng_chs.txt"))))) {
            Map<String, BigDictValue> chsEngMap = new TreeMap<>();

            String line;
            while ((line = br.readLine()) != null) {
                if (line.strip().startsWith("#")) continue;
                
                String[] split = line.split("\s{3}");
                List<String> stripped = Arrays.stream(split)
                        .map(String::strip)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList());
                if (stripped.size() >= 2) {
                    String eng = stripped.get(0);
                    List<String> desPart = new ArrayList<>();
                    for (String spl : stripped.subList(1, stripped.size())) {
                        List<String> spl2 = Arrays.stream(spl.split("\s"))
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
                            posesList.addAll(Arrays.stream(last).toList().subList(0, last.length - 1));
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

    public Result translateOneWord(String sentence) {
        Trie.Get<BigDictValue> results = chsEngTrie.get(sentence);
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

    public Result translateOneWord2(String sentence) {
        Trie.Get<BigDictValue> results = chsEngTrie.get(sentence);
        System.out.println("======");
        Purity best = null;
        for (Map.Entry<String, Trie.Match<BigDictValue>> entry : results.value.entrySet()) {
            Purity bestPosDes = posDesOfMaxPurity(
                    sentence.substring(0, entry.getValue().matchLength), 
                    entry.getValue()
            );
            System.out.println(entry + " " + bestPosDes);
            if (best == null) best = bestPosDes;
            else if (bestPosDes.betterThan(best)) best = bestPosDes;
        }
        System.out.println(best);
        return null;
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

    public static class Result {
        public final String translated;
        public final String partOfSpeech;
        public final int matchLength;

        Result(String translated, String partOfSpeech, int matchLength) {
            this.translated = translated;
            this.partOfSpeech = partOfSpeech;
            this.matchLength = matchLength;
        }

        @Override
        public String toString() {
            return String.format("(%s)%s@%d", partOfSpeech, translated, matchLength);
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
}
