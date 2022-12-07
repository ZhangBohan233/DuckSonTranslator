package trashsoftware.duckSonTranslator.translators;

import trashsoftware.duckSonTranslator.dict.Util;
import trashsoftware.duckSonTranslator.result.TranslationResult;

import java.util.Map;
import java.util.Set;

public abstract class Translator {

    public static final Set<String> NO_SPACE_BEFORE = Set.of(
            "pun", "etc", "unk"
    );
    public static final Set<String> NO_SPACE_AFTER = Set.of(
            "etc", "unk"
    );
    public static final Set<Character> ETC = Set.of(
            '\n', '\t', '\r'
    );
    public static final Set<String> GRAMMAR_TERMINATOR = Util.mergeSets(
            NO_SPACE_BEFORE, NO_SPACE_AFTER, Set.of("num")
    );
    private static final Map<Character, Character> PUNCTUATIONS_REGULAR = Map.of(
            '，', ',', '。', '.', '：', ':', '；', ';',
            '！', '!', '？', '?', '、', ',', '·', ' '
    );
    private static final Map<Character, Character> PUNCTUATIONS_QUOTE = Map.of(
            '“', '"', '”', '"', '‘', '\'', '’', '\'',
            '《', '"', '》', '"', '【', '[', '】', ']',
            '『', '"', '』', '"'
    );
    public static final Map<Character, Character> CHS_PUNCTUATIONS = Util.mergeMaps(
            PUNCTUATIONS_REGULAR, PUNCTUATIONS_QUOTE
    );
    public static final Map<Character, Character> ENG_PUNCTUATIONS =
            Util.invertMap(CHS_PUNCTUATIONS);

    protected DuckSonTranslator parent;

    protected Translator(DuckSonTranslator parent) {
        this.parent = parent;
    }
    
    public abstract TranslationResult translate(String text);
    
    protected static String[] splitToN(String orig, int n) {
        if (orig.length() < n) throw new RuntimeException();
        int avg = orig.length() / n;

        String[] res = new String[n];
        int index = 0;
        for (int i = 0; i < n; i++) {
            String part;
            if (i == n - 1) {
                part = orig.substring(index);
            } else {
                part = orig.substring(index, index + avg);
                index += avg;
            }
            res[i] = part;
        }
        return res;
    }
}
