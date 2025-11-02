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

    public static <K, V> Map<V, List<K>> invertNonBijectionMap(Map<K, V> map) {
        Map<V, List<K>> res = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            List<K> lst = res.computeIfAbsent(entry.getValue(), v -> new ArrayList<>());
            lst.add(entry.getKey());
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

    /**
     * isSubsequence("ABCD", "BC");  // true
     * isSubsequence("ABCD", "AD");  // true
     * isSubsequence("ABCD", "DC");  // false
     * isSubsequence("HELLO", "HLO"); // true
     * isSubsequence("HELLO", "OLH"); // false
     * 
     * @param text    the full text
     * @param pattern the thing to be matched
     * @return is subsequence or not
     */
    public static boolean isSubsequence(String text, String pattern) {
        if (pattern == null || text == null) return false;
        if (pattern.isEmpty()) return true;

        int i = 0; // index for text
        int j = 0; // index for pattern

        while (i < text.length() && j < pattern.length()) {
            if (text.charAt(i) == pattern.charAt(j)) {
                j++; // move to next pattern character when matched
            }
            i++; // always move forward in text
        }

        // If we've matched all pattern characters, it's contained
        return j == pattern.length();
    }
}
