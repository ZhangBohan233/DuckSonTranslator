package trashsoftware.duckSonTranslator.dict;

import trashsoftware.duckSonTranslator.trees.Trie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class BigDict {
    
    protected final Trie<BigDictValue> chsEngTrie = new Trie<>();
    protected final Map<String, BigDictValue> engChsMap = new HashMap<>();
    protected final Map<String, BigDictValue> chsEngMap = new HashMap<>();

    public BigDict() throws IOException {
        readHighSchoolDict();
    }

    private static String standardizeChs(String chsWord) {
        if (chsWord.length() > 1 && chsWord.endsWith("的")) {
            chsWord = chsWord.substring(0, chsWord.length() - 1);
        }

        if (chsWord.length() > 1 && chsWord.startsWith("使")) {
            chsWord = chsWord.substring(1);
        }

        return chsWord;
    }

    private static String replaceWeirdPos(String orig) {
        if (orig.startsWith("*")) orig = orig.substring(1);

        if (orig.startsWith("v")) return "v";
        if (orig.equals("a")) return "adj";
        if (orig.equals("ad")) return "adv";

        return orig;
    }

    private static String addSpaceToFoolishOfHighSchoolTeacher(String s) {
        if (s.length() == 0) return s;
        StringBuilder builder = new StringBuilder();
        char last = s.charAt(0);
        for (char c : s.toCharArray()) {
            if ((last > 255 || last == ')' || last == ']') && c >= 'a' && c <= 'z') {
                builder.append(' ');
            }
            builder.append(c);
            last = c;
        }
        return builder.toString();
    }

    private static String removeInsideParenthesis(String s) {
        boolean inPar = false;
        boolean inSquare = false;
        boolean inArrow = false;
        StringBuilder builder = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '(':
                    inPar = true;
                    break;
                case ')':
                    inPar = false;
                    break;
                case '[':
                    inSquare = true;
                    break;
                case ']':
                    inSquare = false;
                    break;
                case '<':
                    inArrow = true;
                    break;
                case '>':
                    inArrow = false;
                    break;
                default:
                    if (!inPar && !inSquare && !inArrow) {
                        builder.append(c);
                    }
            }
        }
        return builder.toString();
    }

    private void readHighSchoolDict() throws IOException {
        List<String[]> csvContent = DictMaker.readCsv(
                DictMaker.class.getResourceAsStream("voc.csv")
        );
        for (String[] line : csvContent) {
            String eng = line[1].strip().toLowerCase(Locale.ROOT);
            for (int c = 2; c < line.length; c++) {
                String rawDes = line[c].strip();
                if (!rawDes.isEmpty()) {
                    if (rawDes.startsWith("[同]")) continue;
                    rawDes = rawDes.replace(" ", "+");
                    rawDes = removeInsideParenthesis(rawDes);
                    rawDes = addSpaceToFoolishOfHighSchoolTeacher(rawDes);
                    String[] sameMeanDiffPosSplit = rawDes.split("\\./");
                    String[] posMeans;
                    Set<String> sameMeaningDivision = new HashSet<>();
                    if (sameMeanDiffPosSplit.length >= 2) {
                        // 有那种多个词性同义的
                        String[] dumb = sameMeanDiffPosSplit[sameMeanDiffPosSplit.length - 1].split(" ");
                        if (dumb.length != 1) {
                            // 把两个词性放在一个格子里了
                            // 处理方式: 归位
                            int ncStart = c + 1;
                            for (int nc = c + 1; nc < line.length; nc++) {
                                if (line[nc].strip().isBlank()) {
                                    ncStart = nc;
                                    break;
                                }
                            }
                            for (int nc = 1; nc < dumb.length; nc++) {
                                int cInLine = nc + ncStart - 1;
                                line[cInLine] = dumb[nc];
                            }
                        }
                        String[] lastPosAndMean = dumb[0].split("\\.");
                        if (lastPosAndMean.length != 2) {
                            throw new RuntimeException("Unknown syntax at " + eng);
                        }
                        String lastPos = lastPosAndMean[0];
                        String des = lastPosAndMean[1];
//                        System.out.println(Arrays.toString(lastPosAndMean));
                        posMeans = new String[sameMeanDiffPosSplit.length * 2];
                        for (int p = 0; p < sameMeanDiffPosSplit.length - 1; p++) {
                            String pos = sameMeanDiffPosSplit[p];
                            posMeans[p * 2] = pos;
                            sameMeaningDivision.add(pos);
                            posMeans[p * 2 + 1] = des;
                        }
                        posMeans[posMeans.length - 2] = lastPos;
                        posMeans[posMeans.length - 1] = des;
                    } else {
                        posMeans = sameMeanDiffPosSplit[0].split("\\.");
                        List<String> parts = new ArrayList<>();
                        for (String s : posMeans) {
                            String[] spaceSplit = s.split(" ");
                            parts.addAll(Arrays.asList(spaceSplit));
                        }
                        posMeans = parts.toArray(new String[0]);
//                        System.out.println(posMeans.length + Arrays.toString(posMeans));
                        if (posMeans.length % 2 != 0) {
//                            System.out.println(line[c]);
                            throw new RuntimeException(Arrays.toString(posMeans) + " at line " + line[c]);
                        }
                    }

                    BigDictValue engAsKey = engChsMap.computeIfAbsent(eng,
                            k -> new BigDictValue(new HashMap<>()));  // 因为eng可能被之前的列加了
                    engAsKey.sameMeaningDivision.addAll(sameMeaningDivision);
                    Map<String, List<String>> posMapChsValue = engAsKey.value;  // pos: 中文释义
                    for (int cc = 0; cc < posMeans.length; cc += 2) {
                        // 长度已经确定偶数了
                        String pos = replaceWeirdPos(posMeans[cc]);

                        String[] chsDes = posMeans[cc + 1].split(";");
                        List<String> chsDesList = new ArrayList<>();
                        if (posMapChsValue.containsKey(pos)) {
                            chsDesList.addAll(posMapChsValue.get(pos));
                        }
                        for (String chs : chsDes) {
                            chs = standardizeChs(chs.strip());
                            if (!chs.isEmpty()) {
                                chsDesList.add(chs);
                            }
                        }
                        if (!chsDesList.isEmpty()) {
                            posMapChsValue.put(pos, chsDesList);
                        }
                    }
                    engChsMap.put(eng, new BigDictValue(posMapChsValue));

                    for (var posDes : posMapChsValue.entrySet()) {
                        String pos = posDes.getKey();  // pos
                        for (String chs : posDes.getValue()) {
                            BigDictValue chsKey = chsEngMap.get(chs);
                            if (chsKey == null) {
                                chsKey = new BigDictValue(new HashMap<>());
                                chsEngMap.put(chs, chsKey);
                            }
                            List<String> posEngMap =
                                    chsKey.value.computeIfAbsent(pos, k -> new ArrayList<>());
                            if (!posEngMap.contains(eng)) {
                                posEngMap.add(eng);
                            }
//                            System.out.println(chs + posEngMap);
                        }
                    }
                }
            }
        }
        for (Map.Entry<String, BigDictValue> entry : chsEngMap.entrySet()) {
            chsEngTrie.insert(entry.getKey(), entry.getValue());
        }
//        System.out.println(engChsMap.get("feel"));
//        System.out.println(getAllMatches('蝇'));
//        System.out.println(chsEngMap.get("因此"));
//        System.out.println(engChsMap.size());
//        System.out.println(chsEngMap);
    }

    private void readFullDict() throws IOException {
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

    public String getVersionStr() {
        return String.valueOf(engChsMap.size());
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
}
