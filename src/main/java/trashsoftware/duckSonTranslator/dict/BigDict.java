package trashsoftware.duckSonTranslator.dict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class BigDict implements Serializable {

    private static BigDict instanceHighSchool;
    private static BigDict instanceHuge;

    protected final Map<String, BigDictValue> engChsMap = new HashMap<>();
    protected final Map<String, BigDictValue> chsEngMap = new HashMap<>();

    /**
     * 每个英文单词最有代表性的中文字
     */
    protected final Map<String, List<Character>> engChsMostRep = new HashMap<>();

    /**
     * 根据上一个算出来的reverse
     */
    protected final Map<Character, List<String>> chsEngMostRep = new HashMap<>();

    /**
     * 中文字符与词条的表，用于加速搜索
     */
    protected final Map<Character, Map<String, BigDictValue>> chsCharEngMap = new HashMap<>();

    /**
     * 英文字母与词条的表，用于加速搜索
     */
    protected final Map<Character, Map<String, BigDictValue>> engCharChsMap = new HashMap<>();

    protected final Map<String, BigDictValue> engChsHugeMap = new HashMap<>();
    protected final Map<String, BigDictValue> chsEngHugeMap = new HashMap<>();
    protected final Map<Character, Map<String, BigDictValue>> chsCharEngHugeMap = new HashMap<>();
    protected final Map<Character, Map<String, BigDictValue>> engCharChsHugeMap = new HashMap<>();

    private BigDict(boolean hugeDict) throws IOException {
        readHighSchoolDict();

        if (hugeDict) {
            readHugeDict();
        }
    }

    public static BigDict getInstance(boolean containHugeDict) throws IOException {
        if (containHugeDict) {
            if (instanceHuge == null) {
                instanceHuge = new BigDict(true);
            }
            return instanceHuge;
        } else {
            if (instanceHighSchool == null) {
                instanceHighSchool = new BigDict(false);
            }
            return instanceHighSchool;
        }
    }

    private static String standardizeChs(String chsWord) {
        if (chsWord.length() > 1 && chsWord.endsWith("的")) {
            chsWord = chsWord.substring(0, chsWord.length() - 1);
        }

        if (chsWord.length() > 1 && chsWord.startsWith("使")) {
            chsWord = chsWord.substring(1);
        }

        chsWord = chsWord.replaceAll("…", "");

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
        if (s.isEmpty()) return s;
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

    private static void createCharMap(Map<String, BigDictValue> srcMap,
                                      Map<Character, Map<String, BigDictValue>> dstMap) {
        for (var wordValue : srcMap.entrySet()) {
            String word = wordValue.getKey();
            BigDictValue value = wordValue.getValue();
            for (char c : word.toCharArray()) {
                Map<String, BigDictValue> wordsContainThis =
                        dstMap.computeIfAbsent(c, k -> new HashMap<>());
                wordsContainThis.putIfAbsent(word, value);
            }
        }
    }

    private static void createRepresentativeMap(Map<String, BigDictValue> engChsMap,
                                                Map<String, List<Character>> engChsMostRep) {

        class ChsWordItem {
            final char chs;
            final int[] countIndex;  // [计数，index的和]

            ChsWordItem(char chs, int[] countIndex) {
                this.chs = chs;
                this.countIndex = countIndex;
            }
        }

        Map<String, List<ChsWordItem>> rawRepMap = new HashMap<>();

        for (var entry : engChsMap.entrySet()) {
            BigDictValue meanings = entry.getValue();
            Map<Character, int[]> counts = new TreeMap<>();  // 值为: [计数，index的和]
            for (var posDes : meanings.value.entrySet()) {
                Set<String> desLst = posDes.getValue();
                for (String des : desLst) {
                    for (int i = 0; i < des.length(); i++) {
                        char c = des.charAt(i);
                        int[] vals = counts.computeIfAbsent(c, k -> new int[2]);
                        vals[0]++;  // 计数+1
                        vals[1] += i;  // 出现的index + 1
                    }
                }
            }
            List<ChsWordItem> chsChars = counts.entrySet().stream().map(ent ->
                    new ChsWordItem(ent.getKey(), ent.getValue())).collect(Collectors.toList());
            if (chsChars.isEmpty()) continue;
            Comparator<ChsWordItem> priceCmp = (o1, o2) -> {
                int countCmp = Integer.compare(o1.countIndex[0], o2.countIndex[0]);
                if (countCmp != 0) return -countCmp;
                return 0;
            };
            chsChars.sort(priceCmp);
            ChsWordItem first = chsChars.get(0);
            List<ChsWordItem> res = new ArrayList<>();
            res.add(first);
            for (int i = 1; i < chsChars.size(); i++) {
                ChsWordItem charI = chsChars.get(i);
                if (priceCmp.compare(first, charI) == 0) {
                    res.add(charI);
                } else break;
            }
            rawRepMap.put(entry.getKey(), res);
        }
        
        // 相当于是减少歧义
        Map<Character, Integer> countRawRepChs = new HashMap<>();  // 每个汉字被选为代表的次数
        for (var entry : rawRepMap.entrySet()) {
            for (ChsWordItem cwi : entry.getValue()) {
                countRawRepChs.merge(cwi.chs, 1, Integer::sum);
            }
        }
        for (var entry : rawRepMap.entrySet()) {
            List<ChsWordItem> cwiList = entry.getValue();
            Comparator<ChsWordItem> priceCmp2 = (o1, o2) -> {
                // 在这里，countIndex[0]一定相等
                int freqOther1 = countRawRepChs.get(o1.chs);
                int freqOther2 = countRawRepChs.get(o2.chs);
                int commonCmp = Integer.compare(freqOther1, freqOther2);  // 相当于是这个字的独特性
                if (commonCmp != 0) {
                    return commonCmp;  // 越小越好
                }
                return Integer.compare(o1.countIndex[1], o2.countIndex[1]);
            };
            cwiList.sort(priceCmp2);

            ChsWordItem first = cwiList.get(0);
            List<Character> res = new ArrayList<>();
            res.add(first.chs);
            for (int i = 1; i < cwiList.size(); i++) {
                ChsWordItem charI = cwiList.get(i);
                if (priceCmp2.compare(first, charI) == 0) {
                    res.add(charI.chs);
                } else break;
            }
            engChsMostRep.put(entry.getKey(), res);
        }
//        System.out.println(countRawRepChs.get('最') + " " + countRawRepChs.get('少'));
    }

    private static void createReverseRepMap(Map<String, List<Character>> engChsMostRep,
                                            Map<Character, List<String>> revMap) {
        for (var entry : engChsMostRep.entrySet()) {
            for (Character chs : entry.getValue()) {
                List<String> got = revMap.computeIfAbsent(chs, k -> new ArrayList<>());
                got.add(entry.getKey());
            }
        }
    }

    private static void copyTo(Map<String, BigDictValue> src, Map<String, BigDictValue> dst) {
        for (var entry : src.entrySet()) {
            dst.put(entry.getKey(), entry.getValue().copy());
        }
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
//                    engAsKey.sameMeaningDivision.addAll(sameMeaningDivision);
                    Map<String, Set<String>> posMapChsValue = engAsKey.value;  // pos: 中文释义
                    for (int cc = 0; cc < posMeans.length; cc += 2) {
                        // 长度已经确定偶数了
                        String pos = replaceWeirdPos(posMeans[cc]);

                        String[] chsDes = posMeans[cc + 1].split(";");
                        Set<String> chsDesList = new HashSet<>();
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
                            BigDictValue chsKey = chsEngMap.computeIfAbsent(chs, k -> new BigDictValue(new HashMap<>()));
                            Set<String> posEngMap =
                                    chsKey.value.computeIfAbsent(pos, k -> new HashSet<>());
                            posEngMap.add(eng);
//                            System.out.println(chs + posEngMap);
                        }
                    }
                }
            }
        }
        createCharMap(chsEngMap, chsCharEngMap);
        createCharMap(engChsMap, engCharChsMap);

        createRepresentativeMap(engChsMap, engChsMostRep);
        createReverseRepMap(engChsMostRep, chsEngMostRep);

//        System.out.println("Rep: " + chsEngMostRep);
    }

    private void readHugeDict() throws IOException {
        copyTo(engChsMap, engChsHugeMap);
        copyTo(chsEngMap, chsEngHugeMap);

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
                        Map<String, Set<String>> posDes = new TreeMap<>();
                        String[] poses = posDesPart.split("\\.,");
                        if (poses.length == 1) {
                            poses = poses[0].split("\\.");
                            for (int i = 0; i < poses.length; i += 2) {
                                posDes.put(
                                        poses[i],
                                        Arrays.stream(poses[i + 1].split("，"))
                                                .map(s -> standardizeChs(s.strip()))
                                                .collect(Collectors.toSet())
                                );
                            }
                        } else {
                            String[] last = poses[poses.length - 1].split("\\.");
                            List<String> posesList = new ArrayList<>(List.of(poses).subList(0, poses.length - 1));
                            posesList.addAll(Arrays.asList(last).subList(0, last.length - 1));

//                            posesList.addAll(Arrays.stream(last).toList().subList(0, last.length - 1));
                            Set<String> des = Arrays.stream(last[last.length - 1]
                                            .split("，"))
                                    .map(s -> standardizeChs(s.strip()))
                                    .collect(Collectors.toSet());
                            for (String pos : posesList) {
                                posDes.put(pos, des);
                            }
                        }

                        // 融合
                        BigDictValue sameWordDiffPos = engChsHugeMap.get(eng);
                        if (sameWordDiffPos == null) {
                            engChsHugeMap.put(eng, new BigDictValue(posDes));
                        } else {
//                            sameWordDiffPos.value.putAll(posDes);
                            for (var posDesEntry : posDes.entrySet()) {
                                String pos = posDesEntry.getKey();
                                Set<String> newDes = posDesEntry.getValue();
                                Set<String> oldDes = sameWordDiffPos.value.computeIfAbsent(pos,
                                        k -> new HashSet<>());
                                for (String s : newDes) {
                                    oldDes.add(s);
                                }
                            }
                        }
//                        System.out.println(eng + " " + engChsMap.get(eng));

                        for (Map.Entry<String, Set<String>> pd : posDes.entrySet()) {
                            for (String des : pd.getValue()) {
                                BigDictValue val = chsEngHugeMap.get(des);
                                if (val == null) {
                                    Map<String, Set<String>> engPosDes = new TreeMap<>();
                                    Set<String> engDes = new HashSet<>();
                                    engDes.add(eng);
                                    engPosDes.put(pd.getKey(), engDes);
                                    val = new BigDictValue(engPosDes);
                                    chsEngHugeMap.put(des, val);
                                } else {
                                    Set<String> engDes = val.value.get(pd.getKey());
                                    if (engDes == null) {
                                        engDes = new HashSet<>();
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
            createCharMap(chsEngHugeMap, chsCharEngHugeMap);
            createCharMap(engChsHugeMap, engCharChsHugeMap);
//            chsKeyHugeTrie = new Trie<>();
//            for (var entry : chsEngHugeMap.entrySet()) {
//                chsKeyHugeTrie.insert(entry.getKey(), entry.getValue());
//            }
//            for (Map.Entry<String, BigDictValue> entry : chsEngMap.entrySet()) {
//                chsEngTrie.insert(entry.getKey(), entry.getValue());
//            }
//            System.out.println(engChsHugeMap);
//            System.out.println(chsEngTrie.get("经济学人"));
        }
    }

    public String getVersionStr() {
        return String.valueOf(engChsMap.size());
    }

//    public Trie<BigDictValue> getChsEngTrie() {
//        return chsEngTrie;
//    }

    public Map<String, BigDictValue> getEngChsMap() {
        return engChsMap;
    }

    public Map<String, BigDictValue> getChsEngMap() {
        return chsEngMap;
    }

    public Map<String, BigDictValue> getChsEngHugeMap() {
        return chsEngHugeMap;
    }

    public BigDictValue getByChs(String chs, boolean hugeDict) {
        Map<String, BigDictValue> dict = hugeDict ? chsEngHugeMap : chsEngMap;
        return dict.get(chs);
    }

    public BigDictValue getByEng(String eng, boolean hugeDict) {
        Map<String, BigDictValue> dict = hugeDict ? engChsHugeMap : engChsMap;
        return dict.get(eng);
    }

    public Map<String, BigDictValue> getEngChsHugeMap() {
        return engChsHugeMap;
    }

    public Map<String, BigDictValue> getAllChsMatches(char chs) {
        return getAllChsMatches(chs, false);
    }

    public Map<String, BigDictValue> getAllChsMatches(char chs, boolean hugeDict) {
        Map<Character, Map<String, BigDictValue>> dict = hugeDict ? chsCharEngHugeMap : chsCharEngMap;
        Map<String, BigDictValue> allMatches = dict.get(chs);
        return allMatches == null ? new HashMap<>() : allMatches;
    }

    public List<Character> mostRepChsByEng(String eng) {
        return engChsMostRep.get(eng);
    }

    public boolean hasChs(char chs, boolean hugeDict) {
        Map<Character, Map<String, BigDictValue>> dict = hugeDict ? chsCharEngHugeMap : chsCharEngMap;
        return dict.containsKey(chs);
    }

    public Map<String, BigDictValue> getAllEngMatches(char c, boolean hugeDict) {
        Map<Character, Map<String, BigDictValue>> dict = hugeDict ? engCharChsHugeMap : engCharChsMap;
        Map<String, BigDictValue> allMatches = dict.get(c);
        return allMatches == null ? new HashMap<>() : allMatches;
    }

    /**
     * 返回所有在index位是c的词条
     */
    private Map<String, BigDictValue> matchAtIndex(CharMatchFinder finder, char c, int index, boolean hugeDict) {
        Map<String, BigDictValue> result = new HashMap<>();
        Map<String, BigDictValue> matches = finder.find(c, hugeDict);
        for (var entry : matches.entrySet()) {
            if (entry.getKey().indexOf(c) == index) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private WordMatch findSubstringMatches(CharMatchFinder finder,
                                           String sentence,
                                           boolean hugeDict) {
        Map<String, BigDictValue> matches = new HashMap<>();
        for (int i = 0; i < sentence.length(); i++) {
            char c = sentence.charAt(i);
            Map<String, BigDictValue> allContainC = finder.find(c, hugeDict);
            matches = matches.isEmpty() ? allContainC : Util.intersection(matches, allContainC);
        }
        Map<String, BigDictValue> correctOrder = new HashMap<>();
        for (var entry : matches.entrySet()) {
            if (Util.isSubsequence(entry.getKey(), sentence)) {
                correctOrder.put(entry.getKey(), entry.getValue());
            }
        }
        return new WordMatch(sentence.length(), correctOrder);
    }

    private WordMatch findPrefixMatches(CharMatchFinder finder, String sentence, boolean hugeDict, boolean requireExact) {
        Map<String, BigDictValue> matches = new HashMap<>();
        for (int i = 0; i < sentence.length(); i++) {
            char c = sentence.charAt(i);
            Map<String, BigDictValue> charMatchAtI = matchAtIndex(finder, c, i, hugeDict);

            Map<String, BigDictValue> intersection = Util.intersection(matches, charMatchAtI);
            if (i == 0) {
                if (charMatchAtI.isEmpty()) return null;
                else matches = charMatchAtI;
            } else if (intersection.isEmpty()) {
                if (requireExact) return null;
                else return new WordMatch(i, matches);
            } else {
                matches = intersection;
            }
        }
        return new WordMatch(sentence.length(), matches);
    }

    /**
     * 返回所有与sentence有共同子串的match
     *
     * @param requireExact sentence是否必须为返回词的完全子串。例如“高速路”不是"高速公路"的完全子串
     */
    public WordMatch findPrefixMatchesByChs(String sentence, boolean hugeDict, boolean requireExact) {
        return findPrefixMatches(this::getAllChsMatches, sentence, hugeDict, requireExact);
    }

    public WordMatch findPrefixMatchesByEng(String sentence, boolean hugeDict, boolean requireExact) {
        return findPrefixMatches(this::getAllEngMatches, sentence, hugeDict, requireExact);
    }

    public WordMatch findSubstringMatchesByChs(String sentence,
                                               boolean hugeDict) {
        return findSubstringMatches(this::getAllChsMatches, sentence, hugeDict);
    }

    public WordMatch findSubstringMatchesByEng(String sentence,
                                               boolean hugeDict) {
        return findSubstringMatches(this::getAllEngMatches, sentence, hugeDict);
    }

    private interface CharMatchFinder {
        Map<String, BigDictValue> find(char c, boolean hugeDict);
    }

    public static class WordMatch {
        public final int length;
        public final Map<String, BigDictValue> matches;

        WordMatch(int length, Map<String, BigDictValue> matches) {
            this.length = length;
            this.matches = matches;
        }

        @Override
        public String toString() {
            return "WordMatch{" +
                    "length=" + length +
                    ", matches=" + matches +
                    '}';
        }
    }
}
