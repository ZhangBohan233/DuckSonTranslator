package trashsoftware.duckSonTranslator.dict;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BigDictValue {
    
    public final Set<String> sameMeaningDivision;  // 如那种 v./n.
    public final Map<String, List<String>> value;  // 词性:[释义]
    
    BigDictValue(Map<String, List<String>> value) {
        this(value, new HashSet<>());
    }

    BigDictValue(Map<String, List<String>> value, Set<String> sameMeaningDivision) {
        this.value = value;
        this.sameMeaningDivision = sameMeaningDivision;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
