package trashsoftware.duckSonTranslator.dict;

import java.io.*;
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

    public static final Map<Character, char[]> TONE_DICT = mergeMaps(
            TONE_DICT_A, TONE_DICT_O, TONE_DICT_E, TONE_DICT_I, TONE_DICT_U, TONE_DICT_V
    );

    @SafeVarargs
    private static <K extends Comparable<K>, V> Map<K, V> mergeMaps(Map<K, V>... maps) {
        Map<K, V> res = new TreeMap<>();
        for (Map<K, V> map : maps) {
            res.putAll(map);
        }
        return res;
    }

    public static List<String[]> readCsv(InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String[]> res = new ArrayList<>();
            int lineNum = 0;
            int nCol = -1;
            String line;
            while ((line = br.readLine()) != null) {
                lineNum++;
                line = line.strip();
                if (!(line.isBlank() || line.startsWith("#"))) {
                    String[] split = line.split(",");
                    if (nCol == -1) nCol = split.length;
                    else if (nCol != split.length)
                        throw new IOException(
                                "Csv widths not consistent at line " + lineNum);
                    for (int i = 0; i < nCol; i++) {
                        split[i] = split[i].strip();
                    }
                    res.add(split);
                }
            }
            return res;
        }
    }

    public static Map<Character, String> getChsPinyinDict() throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(
                        DictMaker.class.getResourceAsStream("pinyin.txt"))))) {
            Map<Character, String> result = new TreeMap<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                for (String s : split) {
                    s = s.strip();
                    if (s.length() > 2) {
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
                        result.put(chs,
                                tone == 0 ?
                                        builder.toString() : builder.append(tone).toString());
                    }
                }
            }
            return result;
        }
    }
    
}
