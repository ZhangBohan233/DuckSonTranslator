package trashsoftware.duckSonTranslator.trees;

import java.util.HashMap;
import java.util.Map;

public class TrieNode<V> extends TreeNode<TrieNode<V>, V> {

    protected String fullWord;
    protected int depth;  // 相当于这个node在整个string里的index + 1
    protected int beginIndex;  // 对于trie来说是0，对suffixTree有用

    protected TrieNode(String fullWord,
                       char ch, 
                       Tree<TrieNode<V>, V> tree, 
                       TrieNode<V> parent, 
                       int depth, 
                       int beginIndex, 
                       V value) {
        super(ch, tree, parent, value);

        this.fullWord = fullWord;
        this.beginIndex = beginIndex;
        this.depth = depth;
    }

    protected NodeExact<TrieNode<V>, V> findNode(String fullStr, int index) {
        char finding = fullStr.charAt(index);
        TrieNode<V> child = children == null ? null : children.get(finding);
        if (child == null) {
            return new NodeExact<>(this, false, index);
        } else {
            if (index == fullStr.length() - 1) {
                return new NodeExact<>(child, true, index + 1);
            } else {
                return child.findNode(fullStr, index + 1);
            }
        }
    }

//    protected void insert(String fullStr, V value) {
//        if (depth == fullStr.length()) return;
//        char next = fullStr.charAt(depth);
//        new TrieNode<>(next, tree, this, depth + 1, value);
//    }
    
    protected Map<String, V> allChildren() {
        Map<String, V> resultMap = new HashMap<>();
        if (getValue() != null) {
            resultMap.put(fullWord(), getValue());
        }
        if (children != null) {
            for (TrieNode<V> sub : children.values()) {
                resultMap.putAll(sub.allChildren());
            }
        }
        return resultMap;
    }

    protected void allChildrenWithValue(Map<String, Trie.Match<V>> map, int matchLength) {
        if (getValue() != null) {
            map.put(fullWord(), new Trie.Match<>(getValue(), matchLength));
        }
        if (children != null) {
            for (TrieNode<V> sub : children.values()) {
                sub.allChildrenWithValue(map, matchLength);
            }
        }
    }
    
    protected int shallowestValuedPath() {
        return shallowestValuedPath(Integer.MAX_VALUE);
    }
    
    protected int shallowestValuedPath(int currentMin) {
        if (depth < currentMin) {
            if (getValue() != null) return depth;
            else {
                if (children == null) return currentMin;
                for (TrieNode<V> child : children.values()) {
                    int cs = child.shallowestValuedPath(currentMin);
                    if (cs < currentMin) currentMin = cs;
                }
                return currentMin;
            }
        } else return currentMin;
    }
    
    protected void shallowestPathChildren(Map<String, Trie.Match<V>> map, int shallow, int matchLength) {
        if (depth <= shallow) {
            if (getValue() != null) {
                map.put(fullWord, new Trie.Match<>(getValue(), matchLength));
            }
            if (children != null) {
                for (TrieNode<V> child : children.values()) {
                    child.shallowestPathChildren(map, shallow, matchLength);
                }
            }
        }
    }

    protected Map<String, Trie.Match<V>> shallowestPathChildren(int matchLength) {
        int shallow = shallowestValuedPath();
        Map<String, Trie.Match<V>> map = new HashMap<>();
        shallowestPathChildren(map, shallow, matchLength);
        return map;
    }

    public int getDepth() {
        return depth;
    }

    public String fullWord() {
        return fullWord;
//        char[] arr = new char[depth + beginIndex - 1];
//        fillFullWord(arr);
//        return new String(arr);
    }

    protected void fillFullWord(char[] array) {
        if (parent != null) {  // implies ch != ' ', depth != 0
            array[depth - 1] = ch;
            parent.fillFullWord(array);
        }
    }
}
