package trashsoftware.duckSonTranslator.dict;

import java.util.List;
import java.util.Map;

public class BigDictValue {
    
    public final Map<String, List<String>> value;  // 词性:[释义]
    
    BigDictValue(Map<String, List<String>> value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
