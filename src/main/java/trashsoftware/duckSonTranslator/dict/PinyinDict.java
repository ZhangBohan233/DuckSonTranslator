package trashsoftware.duckSonTranslator.dict;

import java.io.IOException;
import java.util.*;

public class PinyinDict {

    private static PinyinDict instance;

    protected int cqPinCount = 0;
    protected Map<Character, PinyinItem> pinyin;
    protected Map<String, PinyinItem> nonUtf8Pinyin = new HashMap<>();
//    protected Map<Character, String[]> pinyin;  // 值的长度3, [普通话拼音数字版，重庆拼音，真拼音]
//    protected Map<String, String[]> fullPinyin;  // 补充用的多音字拼音，长度任意。键为String因为unicode有些字符长度不为1
//    protected Map<String, List<String[]>> cantonesePin;  // 用于判断入声用，每个读音有[粤拼，声母，韵母，声调]
    
    // 繁体字表，暂时还没用上
    protected Map<Character, Character> traditionalSimplified;
    protected Map<Character, List<Character>> simplifiedTraditional;

    protected Map<String, List<Character>> pinyinToChs = new HashMap<>();
    protected Map<String, List<Character>> cqPinToChs = new HashMap<>();

    protected PinyinDict() throws IOException {
        pinyin = DictMaker.getChsPinyinDict();
        DictMaker.readFullPinyinDict(pinyin, nonUtf8Pinyin);
        traditionalSimplified = DictMaker.readTraditionalSimplifiedConversion();
        simplifiedTraditional = Util.invertNonBijectionMap(traditionalSimplified);
        Map<String, List<String[]>> cantonesePin = DictMaker.readCantonesePinyin();  // 就用这一次了，放这里省内存
        DictMaker.processRuShengForCqPin(pinyin, cantonesePin);

//        System.out.println(Arrays.toString(fullPinyin.get("压")));

        // 把baseDict里面说明了的重庆拼音写进去
        BaseDict baseDict = BaseDict.getInstance();
        for (var entry : baseDict.chsMap.entrySet()) {
            String word = entry.getKey();
            if (word.length() == 1) {
                char c = word.charAt(0);
                PinyinItem pi = pinyin.get(c);
                if (pi == null) throw new RuntimeException(c + " not have pinyin");
                pi.forceCoverCqPin(entry.getValue().cq.split(";"));
                pi.forceSetDefaultPinyin(entry.getValue().pinyin);
            }
        }
        
        // cq_pin里面的读音是优先级最高的
        // 不应不baseDict里面冲突，但不排除开发者脑壳有bing bong。这种情况下以cq_pin为准
        List<String[]> csv = DictMaker.readCsv(
                DictMaker.class.getResourceAsStream("cq_pin.txt"));
        for (String[] line : csv) {
            if (line[0].length() != 1) throw new RuntimeException("Duck son");
            PinyinItem pi = pinyin.get(line[0].charAt(0));
            if (pi == null) throw new RuntimeException(line[0].charAt(0) + " not have pinyin");
            pi.forceCoverCqPin(line[1].split(";"));
            cqPinCount++;
        }

        makeRevPinyinDict();

        int min = 65536;
        int max = 0;
        for (char c : pinyin.keySet()) {
            if (c < min) min = c;
            if (c > max) max = c;
        }
        System.out.println(pinyin.size() + " " + min + " " + max);
    }

    public static PinyinDict getInstance() throws IOException {
        if (instance == null) {
            instance = new PinyinDict();
        }
        return instance;
    }

    public String getVersionStr() {
        return String.valueOf(cqPinCount);
    }

    private void makeRevPinyinDict() {
        for (Map.Entry<Character, PinyinItem> entry : pinyin.entrySet()) {
            // 多音字仅有第一个读音录入这里
            PinyinItem item = entry.getValue();
            List<Character> samePinyinChar =
                    pinyinToChs.computeIfAbsent(item.getDefaultPinyin(), k -> new ArrayList<>());
            samePinyinChar.add(entry.getKey());

            List<Character> sameCqChar =
                    cqPinToChs.computeIfAbsent(item.getDefaultCqPin(), k -> new ArrayList<>());
            sameCqChar.add(entry.getKey());
        }
    }

    public PinyinItem getPinyinByChs(char ch) {
        return pinyin.get(ch);
    }

    public PinyinItem getPinyinByChs(String nonUtf8) {
        return nonUtf8Pinyin.get(nonUtf8);
    }

//    /**
//     * 返回这个字的全部普通话拼音。注意，这里的String是因为有些生僻字占用2个char，但整个DuckSonTranslator其实是不支持的
//     */
//    public String[] getFullPinyinByChs(String chars) {
//        return fullPinyin.get(chars);
//    }

    public List<Character> getChsListByCqPin(String cqPin) {
        return cqPinToChs.get(cqPin);
    }

    public List<Character> getChsListByPinyin(String pinyin) {
        return pinyinToChs.get(pinyin);
    }

//    public static String getPin(String[] pinyin, boolean chongqingMode) {
//        return chongqingMode ? pinyin[1] : pinyin[0];
//    }

    public List<Character> getSameSoundChsChars(char chs, boolean chongqingMode) {
        PinyinItem item = getPinyinByChs(chs);
        if (item == null) {
            if (Character.isLetter(chs)) {
                // 是英文字母
                return new ArrayList<>(List.of(Character.toUpperCase(chs), Character.toLowerCase(chs)));
            } else {
                return new ArrayList<>(List.of(chs));
            }
        }

//        Set<Character> otherForms = new HashSet<>(simplifiedTraditional.get(chs));
//        otherForms.add(traditionalSimplified.get(chs));
        
        List<Character> sameSound;
        if (chongqingMode) {
            sameSound = getChsListByCqPin(item.getDefaultCqPin());
        } else {
            sameSound = getChsListByPinyin(item.getDefaultPinyin());
        }
        return sameSound;
    }
}
