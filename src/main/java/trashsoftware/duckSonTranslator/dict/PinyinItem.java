package trashsoftware.duckSonTranslator.dict;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PinyinItem {
    
    private final char character;
    private final String nonUtf8;

    /**
     * 这三个用List不用Set的原因是：1.需要顺序，2.读音就这么几个，不会太费时间
     */
    final List<String> pinyinList = new ArrayList<>();  // 数字声调的拼音
    final List<String> cqPinList = new ArrayList<>();
//    final List<String> symbolicPinyinList = new ArrayList<>();  // ā这种形式的拼音
    
    private PinyinItem(char character, String nonUtf8, String symbolicPinyin) {
        this.character = character;
        this.nonUtf8 = nonUtf8;
        
        addPins(symbolicPinyin);
    }
    
    PinyinItem(char character, String symbolicPinyin) {
        this(character, null, symbolicPinyin);
    }
    
    PinyinItem(String text, String symbolicPinyin) {
        this('\0', text, symbolicPinyin);
    }
    
    public static String pinyinSymbolicToNumbered(String symbolicPinyin) {
        int tone = 0;
        StringBuilder builder = new StringBuilder();
        for (char c : symbolicPinyin.toCharArray()) {
            char[] replace = DictMaker.TONE_DICT.get(c);
            if (replace == null) {
                builder.append(c);
            } else {
                builder.append(replace[0]);
                tone = replace[1];
            }
        }
        return tone == 0 ?
                builder.toString() : builder.append(tone).toString();
    }
    
    public static String pinyinNumberedToSymbolic(String numberedPinyin) {
        char toneC = numberedPinyin.charAt(numberedPinyin.length() - 1);
        if (!Character.isDigit(toneC)) {
            if (numberedPinyin.contains("v")) {
                numberedPinyin = numberedPinyin.replace('v', 'ü');
            }
            return numberedPinyin;  // 轻声一样的
        }
        int tone = toneC - '0';
        String pure = numberedPinyin.substring(0, numberedPinyin.length() - 1);
        
        char[] normalVowel = new char[]{'a', 'o', 'e'};
        for (char vowel : normalVowel) {
            int index = pure.indexOf(vowel);
            if (index >= 0) {
                char symbol = DictMaker.TONE_DICT_REV.get("" + vowel + tone);
                return pure.substring(0, index) + symbol + pure.substring(index + 1);
            }
        }
        if (pure.contains("ui")) {
            char symbol = DictMaker.TONE_DICT_REV.get("i" + tone);
            return pure.replace("ui", "u" + symbol);
        }
        if (pure.contains("iu")) {
            char symbol = DictMaker.TONE_DICT_REV.get("u" + tone);
            return pure.replace("iu", "i" + symbol);
        }
        
        // iu和ui之后，检查单独的u和i
        char[] iuVowel = new char[]{'i', 'u'};
        for (char vowel : iuVowel) {
            int index = pure.indexOf(vowel);
            if (index >= 0) {
                char symbol = DictMaker.TONE_DICT_REV.get("" + vowel + tone);
                return pure.substring(0, index) + symbol + pure.substring(index + 1);
            }
        }
        
        int yuIndex = pure.indexOf('v');
        if (yuIndex >= 0) {
            char vowel = 'v';
            char consonant = pure.charAt(0);
            if (consonant == 'j' || consonant == 'q' || consonant == 'x') {
                vowel = 'u';
            }
            char symbol = DictMaker.TONE_DICT_REV.get("" + vowel + tone);
            return pure.substring(0, yuIndex) + symbol + pure.substring(yuIndex + 1);
        }
        throw new RuntimeException("Cannot transfer '" + numberedPinyin + "' to symbolic pinyin");
    }
    
    void addPins(String symbolicPinyin) {
        String numPinyin = pinyinSymbolicToNumbered(symbolicPinyin);
        if (!pinyinList.contains(numPinyin)) {
//            this.symbolicPinyinList.add(symbolicPinyin);

            String pinNum = pinyinSymbolicToNumbered(symbolicPinyin);
            String cqPin = DictMaker.makeDefaultCqPin(pinNum);  // 先假设重庆话和普通话拼音一样
            cqPinList.add(cqPin);
            pinyinList.add(pinNum);
        }
    }
    
    void setRuSheng() {
        String cq = getDefaultCqPin();
        if (Character.isDigit(cq.charAt(cq.length() - 1))) {
            cq = cq.substring(0, cq.length() - 1);
        }
        cq += '2';  // 入声归阳平
        cqPinList.clear();
        cqPinList.add(cq);
    }
    
    void forceCoverCqPin(String[] cqPins) {
        cqPinList.clear();
        for (String s : cqPins) {
            cqPinList.add(s.strip());
        }
    }
    
    void forceSetDefaultPinyin(String numPinyin) {
        if (!pinyinList.contains(numPinyin)) {
            pinyinList.add(0, numPinyin);
//            symbolicPinyinList.add(0, pinyinNumberedToSymbolic(numPinyin));
        }
    }
    
    public boolean isChar() {
        return character != 0;
    }

    public char getCharacter() {
        return character;
    }

    public String getNonUtf8() {
        return nonUtf8;
    }
    
    public String getDefaultPinyin() {
        return pinyinList.get(0);
    }
    
    public String getDefaultCqPin() {
        return cqPinList.get(0);
    }
    
    public boolean isLightSound() {
        String firstPinyin = getDefaultPinyin();
        return Character.isDigit(firstPinyin.charAt(firstPinyin.length() - 1));
    }
    
    public List<String> getSymbolicPinyinList() {
        return pinyinList.stream().map(PinyinItem::pinyinNumberedToSymbolic).collect(Collectors.toList());
    }

    public List<String> getCqPinList() {
        return new ArrayList<>(cqPinList);
    }

    @Override
    public String toString() {
        return "PinyinItem{" +
                "character=" + character +
                ", nonUtf8='" + nonUtf8 + '\'' +
                ", pinyinList=" + pinyinList +
                ", cqPinList=" + cqPinList +
                '}';
    }
}
