package trashsoftware.duckSonTranslator.dict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PinyinDict {
    
    private static PinyinDict instance;

    protected int cqPinCount = 0;
    protected Map<Character, String[]> pinyin;  // 长度2, [拼音，重庆拼音]

    protected Map<String, List<Character>> pinyinToChs = new HashMap<>();
    protected Map<String, List<Character>> cqPinToChs = new HashMap<>();

    protected PinyinDict() throws IOException {
        pinyin = DictMaker.getChsPinyinDict();
        List<String[]> csv = DictMaker.readCsv(
                DictMaker.class.getResourceAsStream("cq_pin.csv"));
        for (String[] line : csv) {
            if (line[0].length() != 1) throw new RuntimeException("Duck son");
            String[] arr = pinyin.get(line[0].charAt(0));
            if (arr == null) throw new RuntimeException(line[0].charAt(0) + " not have pinyin");
            arr[1] = line[1];
            cqPinCount++;
        }

        makeRevPinyinDict();
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

    public List<Character> getChsListByCqPin(String cqPin) {
        return cqPinToChs.get(cqPin);
    }

    public List<Character> getChsListByPinyin(String pinyin) {
        return pinyinToChs.get(pinyin);
    }
}
