package trashsoftware.duckSonTranslator.dict;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BaseDict {
    
    protected final Map<String, BaseItem> chsMap = new TreeMap<>();
    protected final Map<String, BaseItem> cqMap = new TreeMap<>();
    protected final Map<String, BaseItem> pinyinMap = new TreeMap<>();
    protected final Map<String, BaseItem> engMap = new TreeMap<>();
    protected int maxChsWordLen = 0;
    
    public BaseDict() throws IOException {
        List<String[]> csvContent = DictMaker.readCsv(
                DictMaker.class.getResourceAsStream("base.csv")
        );
        for (int i = 1; i < csvContent.size(); i++) {
            String[] line = csvContent.get(i);
            BaseItem bi = new BaseItem(line[0], line[1], line[2], line[3], line[4]);
            
            if (line[0].length() > maxChsWordLen) maxChsWordLen = line[0].length();
            
            chsMap.put(line[0], bi);
            if (!cqMap.containsKey(line[1])) cqMap.put(line[1], bi);
            if (!pinyinMap.containsKey(line[2])) pinyinMap.put(line[2], bi);
            if (!engMap.containsKey(line[3])) engMap.put(line[3], bi);
        }
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
    
    public BaseItem getByPinyin(String pinyin) {
        return pinyinMap.get(pinyin);
    }
}
