package trashsoftware.duckSonTranslator.dict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class DictMaker {


    private static final Map<Character, char[]> TONE_DICT_A = Map.of(
            'ā', new char[]{'a', 1},
            'á', new char[]{'a', 2},
            'ǎ', new char[]{'a', 3},
            'à', new char[]{'a', 4}
    );
    private static final Map<Character, char[]> TONE_DICT_O = Map.of(
            'ō', new char[]{'o', 1},
            'ó', new char[]{'o', 2},
            'ǒ', new char[]{'o', 3},
            'ò', new char[]{'o', 4}
    );
    private static final Map<Character, char[]> TONE_DICT_E = Map.of(
            'ē', new char[]{'e', 1},
            'é', new char[]{'e', 2},
            'ě', new char[]{'e', 3},
            'è', new char[]{'e', 4}
    );
    private static final Map<Character, char[]> TONE_DICT_I = Map.of(
            'ī', new char[]{'i', 1},
            'í', new char[]{'i', 2},
            'ǐ', new char[]{'i', 3},
            'ì', new char[]{'i', 4}
    );
    private static final Map<Character, char[]> TONE_DICT_U = Map.of(
            'ū', new char[]{'u', 1},
            'ú', new char[]{'u', 2},
            'ǔ', new char[]{'u', 3},
            'ù', new char[]{'u', 4}
    );
    private static final Map<Character, char[]> TONE_DICT_V = Map.of(
            'ü', new char[]{'v', 0},
            'ǖ', new char[]{'v', 1},
            'ǘ', new char[]{'v', 2},
            'ǚ', new char[]{'v', 3},
            'ǜ', new char[]{'v', 4}
    );

    public static final Map<Character, char[]> TONE_DICT = Util.mergeMaps(
            TONE_DICT_A, TONE_DICT_O, TONE_DICT_E, TONE_DICT_I, TONE_DICT_U, TONE_DICT_V
    );

    public static List<String[]> readCsv(InputStream inputStream) throws IOException {
        return readCsv(inputStream, false, false);
    }

    public static List<String[]> readDictCsv(String fileName,
                                             boolean withTitle,
                                             boolean allowDiffWidth) throws IOException {
        return readCsv(DictMaker.class.getResourceAsStream(fileName), withTitle, allowDiffWidth);
    }

    public static List<String[]> readCsv(InputStream inputStream,
                                         boolean withTitle,
                                         boolean allowDiffWidth) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String[]> res = new ArrayList<>();
            int lineNum = 0;
            int nCol = -1;
            String line;
            while ((line = br.readLine()) != null) {
                lineNum++;

                int commentBegin = line.indexOf('#');
                if (commentBegin != -1) {
                    line = line.substring(0, commentBegin);
                }

                if (!line.isBlank()) {
                    String[] split = line.split(",", -1);
                    if (nCol == -1) {
                        nCol = split.length;
                        if (!withTitle) continue;
                    } else if (!allowDiffWidth && nCol != split.length)
                        throw new IOException(
                                String.format("Csv widths not consistent at line %d. " +
                                                "Title has %d columns, " +
                                                "while this line has %d. Line content: %s",
                                        lineNum,
                                        nCol,
                                        split.length,
                                        line));
                    for (int i = 0; i < split.length; i++) {
                        split[i] = split[i].strip();
                    }
                    res.add(split);
                }
            }
            return res;
        }
    }

    public static List<String[]> readTsv(InputStream inputStream,
                                         boolean withTitle,
                                         boolean allowDiffWidth) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String[]> res = new ArrayList<>();
            int lineNum = 0;
            int nCol = -1;
            String line;
            while ((line = br.readLine()) != null) {
                lineNum++;

                int commentBegin = line.indexOf('#');
                if (commentBegin != -1) {
                    line = line.substring(0, commentBegin);
                }

                if (!line.isBlank()) {
                    String[] split = line.split("\t", -1);
                    if (nCol == -1) {
                        nCol = split.length;
                        if (!withTitle) continue;
                    } else if (!allowDiffWidth && nCol != split.length)
                        throw new IOException(
                                String.format("Tsv widths not consistent at line %d. " +
                                                "Title has %d columns, " +
                                                "while this line has %d. Line content: %s",
                                        lineNum,
                                        nCol,
                                        split.length,
                                        line));
                    for (int i = 0; i < split.length; i++) {
                        split[i] = split[i].strip();
                    }
                    res.add(split);
                }
            }
            return res;
        }
    }

    public static Map<Character, String[]> getChsPinyinDict() throws IOException {
        try (BufferedReader pinBr = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(
                        DictMaker.class.getResourceAsStream("pinyin.txt"))))) {
            Map<Character, String[]> result = new TreeMap<>();
            String line;
            while ((line = pinBr.readLine()) != null) {
                String[] split = line.split(",");
                for (String s : split) {
                    s = s.strip();
                    if (s.length() >= 2) {
                        char chs = s.charAt(0);
                        String pinyin = s.substring(1);
                        int tone = 0;
                        StringBuilder builder = new StringBuilder();
                        for (char c : pinyin.toCharArray()) {
                            char[] replace = TONE_DICT.get(c);
                            if (replace == null) {
                                builder.append(c);
                            } else {
                                builder.append(replace[0]);
                                tone = replace[1];
                            }
                        }
                        String[] arr = new String[3];
                        arr[0] = tone == 0 ?
                                builder.toString() : builder.append(tone).toString();
                        arr[1] = makeDefaultCqPin(arr[0]);  // 先假设重庆话和普通话拼音一样
                        arr[2] = pinyin;
                        result.put(chs, arr);
                    }
                }
            }
            return result;
        }
    }

    public static Map<String, String[]> readFullPinyinDict() throws IOException {
        try (BufferedReader pinBr = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(
                        DictMaker.class.getResourceAsStream("pinyin_full.txt"))))) {
            Map<String, String[]> result = new HashMap<>();
            String line;
            while ((line = pinBr.readLine()) != null) {
                int hashtagIndex = line.indexOf('#');
                if (hashtagIndex != -1) {
                    line = line.substring(0, hashtagIndex);
                }
                line = line.strip();
                String[] split = line.split(":");
                if (split.length == 2) {
                    String chars = unicodeToChar(split[0].strip());
//                    if (chars.length() != 1) System.err.println(chars + " " + chars.charAt(1) + " " + chars.length());
                    String[] pins = split[1].split(",");
                    for (int i = 0; i < pins.length; i++) {
                        pins[i] = pins[i].strip();
                    }
                    result.put(chars, pins);
                }
            }

            return result;
        }
    }

    public static String unicodeToChar(String unicode) {
        if (unicode == null || !unicode.startsWith("U+")) {
            throw new IllegalArgumentException("Invalid Unicode format. Must start with 'U+'.");
        }

        // Remove the "U+" prefix and parse the code point
        int codePoint = Integer.parseInt(unicode.substring(2), 16);

        // Convert to a String containing the actual character
        return new String(Character.toChars(codePoint));
    }

    public static Map<Character, Character> readTraditionalSimplifiedConversion() throws IOException {
        try (BufferedReader tsBr = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(
                        DictMaker.class.getResourceAsStream("traditional_simplified.txt"))))) {
            Map<Character, Character> result = new HashMap<>();
            String line;
            while ((line = tsBr.readLine()) != null) {
                int i1 = line.indexOf("(");
                int i2 = line.indexOf(")");
                if (i1 > 0 && i2 > i1) {
                    String tra = line.substring(0, i1).strip();
                    String sim = line.substring(i1 + 1, i2).strip();
                    if (tra.length() == 1 && sim.length() == 1) {
                        result.put(tra.charAt(0), sim.charAt(0));
                    }
                }
            }
            return result;
        }
    }
    
    public static Map<String, List<String[]>> readCantonesePinyin() throws IOException {
        List<String[]> tsvContent = readTsv(
                DictMaker.class.getResourceAsStream("jyutping.tsv"),
                false,
                false
        );
        Map<String, List<String[]>> result = new HashMap<>();
        for (String[] line : tsvContent) {
            List<String[]> pins = result.computeIfAbsent(line[0], k -> new ArrayList<>());
            // pin, 声母, 韵母, 声调
           pins.add(new String[]{line[2], line[3], line[4], line[5]});
        }
        return result;
    }
    
    public static void processRuShengForCqPin(Map<Character, String[]> chsCqPin, 
                                               Map<String, List<String[]>> cantonesePin) {
        for (var entry : chsCqPin.entrySet()) {
            String character = String.valueOf(entry.getKey());
            List<String[]> jyutPins = cantonesePin.get(character);  // 有多音字
            if (jyutPins != null) {
                int ruCount = 0;
                for (String[] poly : jyutPins) {
                    String vowel = poly[2];
                    if (vowel.endsWith("p") || vowel.endsWith("t") || vowel.endsWith("k")) {
                        // 入声
                        ruCount++;
                    }
                }
                double ruPercent = (double) ruCount / jyutPins.size();
                
                if (ruPercent > 0.51 || (jyutPins.size() <= 2 && ruPercent > 0)) {
                    // 超过3个读音的多音字半数以上读入声才算
                    String[] chsCq = entry.getValue();
                    if (Character.isDigit(chsCq[1].charAt(chsCq[1].length() - 1))) {
                        chsCq[1] = chsCq[1].substring(0, chsCq[1].length() - 1) + '2';  // 入声归阳平
                    }
                }
            }
        }
    }

    private static String makeDefaultCqPin(String pinyin) {
        char last = pinyin.charAt(pinyin.length() - 1);
        String pure = pinyin;
        String tone = "";
        if (last >= '0' && last <= '4') {
            pure = pinyin.substring(0, pinyin.length() - 1);
            tone = String.valueOf(last);
        }

        if (pure.charAt(0) == 'n') {  // 鼻音
            pure = 'l' + pure.substring(1);
        }

        if (pure.length() > 2) {
            if (pure.charAt(1) == 'h') {  // 翘舌音
                pure = pure.charAt(0) + pure.substring(2);
            }
        }

        if (pure.endsWith("feng") || pure.endsWith("meng")) {  // feng -> fong, meng -> mong
            pure = pure.substring(0, pure.length() - 3) + "ong";
        }

        if (pure.endsWith("eng") || pure.endsWith("ing")) {  // ceng -> cen, xing -> xin
            pure = pure.substring(0, pure.length() - 1);
        }

        if (pure.endsWith("uo")) {  // tuo -> to
            pure = pure.substring(0, pure.length() - 2) + 'o';
        }

        if (pure.equals("hu")) {  // hu -> fu  这里不能用startwith, 因为hui, hun这些
            pure = "fu";
        }

        if (pure.equals("wu")) {  // wu -> vu
            pure = "vu";
        }

        if (pure.equals("lei")) {  // lei -> lui
            pure = "lui";
        }

        return pure + tone;
    }
}
