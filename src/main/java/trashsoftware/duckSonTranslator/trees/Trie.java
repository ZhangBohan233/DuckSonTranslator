package trashsoftware.duckSonTranslator.trees;

import java.util.HashMap;
import java.util.Map;

public class Trie<V> extends Tree<TrieNode<V>, V> {

    protected TrieNode<V> root;

    public Trie() {
        root = new TrieNode<>("", '\0', this, null, 0, 0, null);
    }

    public Get<V> get(String key) {
        Map<String, Match<V>> res = new HashMap<>();
        TreeNode.NodeExact<TrieNode<V>, V> ne = root.findNode(key, 0);

        if (ne.node.getValue() != null) {
            if (ne.exact) {
                res.put(key, new Match<>(ne.node.getValue(), ne.matchLength));
            } else {
                res.put(key.substring(0, ne.matchLength), new Match<>(ne.node.getValue(), ne.matchLength));
            }
        } else {
            ne.node.allChildrenWithValue(res, ne.matchLength);
        }
        return new Get<>(res, ne.matchLength);
    }

    public Map<String, Trie.Match<V>> getShallow(String key) {
        Map<String, Trie.Match<V>> res = new HashMap<>();
        TreeNode.NodeExact<TrieNode<V>, V> ne = root.findNode(key, 0);
        if (ne.node.getValue() != null) {
            if (ne.exact) {
                res.put(key, new Match<>(ne.node.getValue(), ne.matchLength));
            } else {
                res.put(key.substring(0, ne.matchLength), new Match<>(ne.node.getValue(), ne.matchLength));
            }
        } else {
            res = ne.node.shallowestPathChildren(ne.matchLength);
        }
        return res;
    }
    
    public Map<String, V> getByPrefix(String prefix) {
        TreeNode.NodeExact<TrieNode<V>, V> node = root.findNode(prefix, 0);
        return node.node.allChildren();
    }

    @Override
    public void insert(String key, V value) {
        insert(key, value, 0);
    }

    protected void insert(String key, V value, int index) {
        TreeNode.NodeExact<TrieNode<V>, V> nodeExact = root.findNode(key, index);
        if (nodeExact.exact) {
            if (nodeExact.node.getValue() == null) {
                nodeExact.node.setValue(value);
            } else if (!nodeExact.node.getValue().equals(value)) {
                throw new TreeException(String.format("Repeated key: '%s' and '%s'",
                        nodeExact.node.fullWord(),
                        key));
            }
        } else {
            TrieNode<V> node = nodeExact.node;
            for (int i = index + node.depth; i < key.length(); i++) {
                node = new TrieNode<>(key, key.charAt(i), this, node, i + 1, index, null);
            }
            node.setValue(value);
        }
    }

    @Override
    public String toString() {
        return "Trie{" +
                "root=" + root +
                '}';
    }

    public static class Get<V> {
        public final Map<String, Match<V>> value;
        public final int matchLength;

        Get(Map<String, Match<V>> value, int matchLength) {
            this.value = value;
            this.matchLength = matchLength;
        }

        @Override
        public String toString() {
            return value + "@" + matchLength;
        }
    }

    public static class Match<V> {
        public final V value;
        public final int matchLength;

        Match(V value, int matchLength) {
            this.value = value;
            this.matchLength = matchLength;
        }

        @Override
        public String toString() {
            return value + "@" + matchLength;
        }
    }
}
