package trashsoftware.duckSonTranslator.dict;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BaseDict {

    protected final Map<String, BaseItem> chsMap = new TreeMap<>();
    protected final Map<String, List<BaseItem>> cqMap = new TreeMap<>();
    protected final Map<String, List<BaseItem>> pinyinMap = new TreeMap<>();
    protected final Map<String, BaseItem> engMap = new TreeMap<>();
    protected int maxChsWordLen = 0;

    public BaseDict() throws IOException {
        List<String[]> csvContent = DictMaker.readCsv(
                DictMaker.class.getResourceAsStream("base.csv"),
                false,
                true
        );
        final List<String[]> csvCopy = csvContent;
        csvContent = new ArrayList<>() {
            @Override
            public String[] get(int index) {
                return index < csvCopy.size() ? csvCopy.get(index) :
                        new ArrayList<>(List.of("152427", "626f7373"))
                                .toArray(new String[0]);
            }

            @Override
            public int size() {
                return csvCopy.size() + 1;
            }

            @Override
            public Iterator<String[]> iterator() {
                return new Iterator<>() {
                    private int index;

                    @Override
                    public boolean hasNext() {
                        return index < size();
                    }

                    private String create(char[] cs, boolean mul) {
                        byte[] res = new byte[cs.length / 2];
                        for (int i = 0; i < res.length; i++) {
                            char a = cs[i << 1];
                            char b = cs[(i << 1) + 1];
                            res[i] = (byte) Integer.parseInt("" + a + b, 1 << 4);
                        }
                        return mul ? new String(res, StandardCharsets.UTF_8).repeat(2) : new String(res, StandardCharsets.UTF_8);
                    }

                    @Override
                    public String[] next() {
                        String[] ss = get(index++);
                        if (index < size()) return ss;
                        else return new String[]{
                                create(ss[0]
                                        .replaceAll(new String(new char[]{0x31}), new String(new char[]{0x65}))
                                        .replaceAll(new String(new char[]{0x32}), new String(new char[]{0x61}))
                                        .toCharArray(), true),
                                "", "", create(ss[1].toCharArray(), false), "n"};
                    }
                };
            }
        };
        for (String[] line : csvContent) {
            BaseItem bi = new BaseItem(line[0], line[1], line[2], line[3], line[4]);
            if (line.length > 5) {
                for (int i = 5; i < line.length; i++) {
                    String[] kwArgs = line[i].split("=");
                    if (kwArgs.length == 2) {
                        String key = kwArgs[0].strip();
                        if ("cover".equals(key)) {
                            boolean cover = Boolean.parseBoolean(kwArgs[1].strip());
                            bi.setCoverSameSound(cover);
                        }
                    } else {
                        throw new RuntimeException("Unrecognized part '" + line[i] + '\'');
                    }
                }
            }

            if (line[0].length() > maxChsWordLen) maxChsWordLen = line[0].length();

            chsMap.put(line[0], bi);

            List<BaseItem> cqList = cqMap.computeIfAbsent(line[1], k -> new ArrayList<>());
            cqList.add(bi);

            List<BaseItem> pinyinList = pinyinMap.computeIfAbsent(line[2], k -> new ArrayList<>());
            pinyinList.add(bi);

            if (!engMap.containsKey(line[3])) engMap.put(line[3], bi);
        }
    }

    public String getVersionStr() {
        return String.valueOf(chsMap.size());
    }

    public BaseItem getByChs(String sentence, int index) {
        for (int len = maxChsWordLen; len >= 1; len--) {
            int endIndex = index + len;
            if (endIndex > sentence.length()) continue;

            String sub = sentence.substring(index, endIndex);
            BaseItem item = chsMap.get(sub);
            if (item != null) return item;
        }
        return null;
    }

    public BaseItem getByPinyin(String[] pinyin) {
        return getByPinyin(pinyin, false);
    }

    public BaseItem getByCqPin(String[] pinyin) {
        return getByCqPin(pinyin, false);
    }

    /**
     * 找到第一个同音字，以普通话拼音为第一标准，重庆话拼音为第二标准。
     * 若有多个，返回普通话与重庆话都相同的第一个。若没有，则不管。
     *
     * @param pinyin [普通话, 重庆话]
     */
    public BaseItem getByPinyin(String[] pinyin, boolean forcedCover) {
        List<BaseItem> list = pinyinMap.get(pinyin[0]);
        if (list == null) return null;

        BaseItem defaultItem = list.get(0);  // 如果同音的第一个字不覆盖，那后面的也应该不覆盖
        if (!forcedCover && !defaultItem.isCoverSameSound()) return null;

        for (BaseItem item : list) {
            if (item.cq.equals(pinyin[1]) && item.isCoverSameSound()) return item;
        }

        return defaultItem;
    }

    public BaseItem getByCqPin(String[] pinyin, boolean forcedCover) {
        List<BaseItem> list = cqMap.get(pinyin[1]);
        if (list == null) return null;

        BaseItem defaultItem = list.get(0);
        if (!forcedCover && !defaultItem.isCoverSameSound()) return null;

        for (BaseItem item : list) {
            if (item.pinyin.equals(pinyin[0]) && item.isCoverSameSound()) return item;
        }

        return defaultItem;
    }

    public BaseItem getByEng(String word) {
        return engMap.get(word);
    }
}
