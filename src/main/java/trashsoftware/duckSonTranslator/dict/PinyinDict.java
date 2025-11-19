package trashsoftware.duckSonTranslator.dict;

import java.io.IOException;
import java.util.*;

public class PinyinDict {

    private static PinyinDict instance;

    protected int cqPinCount = 0;
    protected Map<Character, String[]> pinyin;  // 值的长度3, [普通话拼音数字版，重庆拼音，真拼音]
    protected Map<String, String[]> fullPinyin;  // 补充用的多音字拼音，长度任意。键为String因为unicode有些字符长度不为1
//    protected Map<String, List<String[]>> cantonesePin;  // 用于判断入声用，每个读音有[粤拼，声母，韵母，声调]
    
    // 繁体字表，暂时还没用上
    protected Map<Character, Character> traditionalSimplified;
    protected Map<Character, List<Character>> simplifiedTraditional;

    protected Map<String, List<Character>> pinyinToChs = new HashMap<>();
    protected Map<String, List<Character>> cqPinToChs = new HashMap<>();

    protected PinyinDict() throws IOException {
        pinyin = DictMaker.getChsPinyinDict();
        fullPinyin = DictMaker.readFullPinyinDict();
        traditionalSimplified = DictMaker.readTraditionalSimplifiedConversion();
        simplifiedTraditional = Util.invertNonBijectionMap(traditionalSimplified);
        Map<String, List<String[]>> cantonesePin = DictMaker.readCantonesePinyin();  // 就用这一次了，放这里省内存
        DictMaker.processRuShengForCqPin(pinyin, cantonesePin);

        System.out.println(Arrays.toString(fullPinyin.get("压")));

        // 把baseDict里面说明了的重庆拼音写进去
        BaseDict baseDict = BaseDict.getInstance();
        for (var entry : baseDict.chsMap.entrySet()) {
            String word = entry.getKey();
            if (word.length() == 1) {
                char c = word.charAt(0);
                String[] arr = pinyin.get(c);
                if (arr == null) throw new RuntimeException(c + " not have pinyin");
                arr[0] = entry.getValue().pinyin;
                arr[1] = entry.getValue().cq;
            }
        }
        
        // cq_pin里面的读音是优先级最高的
        // 不应不baseDict里面冲突，但不排除开发者脑壳有bing bong。这种情况下以cq_pin为准
        List<String[]> csv = DictMaker.readCsv(
                DictMaker.class.getResourceAsStream("cq_pin.txt"));
        for (String[] line : csv) {
            if (line[0].length() != 1) throw new RuntimeException("Duck son");
            String[] arr = pinyin.get(line[0].charAt(0));
            if (arr == null) throw new RuntimeException(line[0].charAt(0) + " not have pinyin");
            arr[1] = line[1];
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
        for (Map.Entry<Character, String[]> entry : pinyin.entrySet()) {
            String[] pinAndCq = entry.getValue();
            List<Character> samePinyinChar =
                    pinyinToChs.computeIfAbsent(pinAndCq[0], k -> new ArrayList<>());
            samePinyinChar.add(entry.getKey());

            List<Character> sameCqChar =
                    cqPinToChs.computeIfAbsent(pinAndCq[1], k -> new ArrayList<>());
            sameCqChar.add(entry.getKey());
        }
    }

    public String[] getPinyinByChs(char ch) {
        return pinyin.get(ch);
    }

    /**
     * 返回这个字的全部普通话拼音。注意，这里的String是因为有些生僻字占用2个char，但整个DuckSonTranslator其实是不支持的
     */
    public String[] getFullPinyinByChs(String chars) {
        return fullPinyin.get(chars);
    }

    public List<Character> getChsListByCqPin(String cqPin) {
        return cqPinToChs.get(cqPin);
    }

    public List<Character> getChsListByPinyin(String pinyin) {
        return pinyinToChs.get(pinyin);
    }

    public static String getPin(String[] pinyin, boolean chongqingMode) {
        return chongqingMode ? pinyin[1] : pinyin[0];
    }

    public List<Character> getSameSoundChsChars(char chs, boolean chongqingMode) {
        String[] pinyin = getPinyinByChs(chs);
        if (pinyin == null) {
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
            sameSound = getChsListByCqPin(getPin(pinyin, true));
        } else {
            sameSound = getChsListByPinyin(getPin(pinyin, false));
        }
        return sameSound;
    }
}
