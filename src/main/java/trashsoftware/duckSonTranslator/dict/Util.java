package trashsoftware.duckSonTranslator.dict;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Util {
    
    public static int countUniqueMeanings(Map<String, List<String>> posDes) {
        Set<String> unique = new HashSet<>();
        for (var entry : posDes.entrySet()) {
            unique.addAll(entry.getValue());
        }
        return unique.size();
    }
}
