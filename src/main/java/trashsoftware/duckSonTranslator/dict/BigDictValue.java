package trashsoftware.duckSonTranslator.dict;

import java.io.Serializable;
import java.util.*;

public class BigDictValue implements Serializable {

    //    public final Set<String> sameMeaningDivision;  // 如那种 v./n.
    public final Map<String, Set<String>> value;  // 词性:[释义]

    BigDictValue(Map<String, Set<String>> value) {
        this.value = value;
//        this.sameMeaningDivision = sameMeaningDivision;
    }
    
    BigDictValue copy() {
        Map<String, Set<String>> newVal = new HashMap<>();
        for (var entry : value.entrySet()) {
            newVal.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return new BigDictValue(newVal);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
