package trashsoftware.duckSonTranslator.dict;

import java.util.*;
import java.util.stream.Collectors;

public class Util {
    
    public static int countUniqueMeanings(Map<String, List<String>> posDes) {
        Set<String> unique = new HashSet<>();
        for (var entry : posDes.entrySet()) {
            unique.addAll(entry.getValue());
        }
        return unique.size();
    }

    public static boolean arrayContains(char[] array, char c) {
        for (char a : array) if (a == c) return true;
        return false;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> mergeMaps(Map<K, V>... maps) {
        Map<K, V> res = new HashMap<>();
        for (Map<K, V> map : maps) {
            res.putAll(map);
        }
        return res;
    }

    public static <K, V> Map<V, K> invertMap(Map<K, V> map) {
        Map<V, K> res = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            res.put(entry.getValue(), entry.getKey());
        }
        return res;
    }
    
    @SafeVarargs
    public static <T> Set<T> mergeSets(Set<T>... sets) {
        Set<T> res = new HashSet<>();
        for (Set<T> set : sets) {
            res.addAll(set);
        }
        return res;
    }

    public static <K, V> Map<K, V> intersection(Map<K, V> m1,
                                                Map<K, V> m2) {
        Map<K, V> result = new HashMap<>();
        for (var entry1 : m1.entrySet()) {
            if (m2.containsKey(entry1.getKey())) {
                result.put(entry1.getKey(), entry1.getValue());
            }
        }
        return result;
    }
    
    public static String listOfDeepArrayToString(List<Object[]> list) {
        return '[' + list.stream().map(Arrays::deepToString).collect(Collectors.joining(", ")) + ']';
    }

    public static String listOfArrayToString(List<int[]> list) {
        return '[' + list.stream().map(Arrays::toString).collect(Collectors.joining(", ")) + ']';
    }
}
