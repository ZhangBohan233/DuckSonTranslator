package trashsoftware.duckSonTranslator.trees;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class TreeNode<T extends TreeNode<T, V>, V> {
    
    public final char ch;
    protected Tree<T, V> tree;
    private V value;
    protected Map<Character, T> children;
    protected T parent;
    
    protected TreeNode(char ch, Tree<T, V> tree, T parent, V value) {
        this.ch = ch;
        this.tree = tree;
        this.parent = parent;
        this.value = value;
        
        tree.nNodes++;
        
        if (parent != null) {
            parent.addChild(ch, this);
        }
        if (this.value != null) {
            tree.size++;
        }
    }
    
    protected TreeNode(char ch, Tree<T, V> tree, T parent) {
        this(ch, tree, parent, null);
    }
    
    protected void addChild(char ch, TreeNode<T, V> child) {
        if (children == null) {
            children = new TreeMap<>();
        }
        children.put(ch, (T) child);
    }

    public char getCh() {
        return ch;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        if (value != null && this.value == null) {
            tree.size++;
        }
        this.value = value;
    }

    @Override
    public String toString() {
        if (children == null) {
            return String.format("%c(%s)", ch, value);
        } else {
            if (value == null) {
                return String.format("%c: %s", ch, children);
            } else {
                return String.format("%c(%s): %s", ch, value, children);
            }
        }
    }

    protected static class NodeExact<T extends TreeNode<T, V>, V> {
        final T node;
        final boolean exact;
        final int matchLength;

        NodeExact(T node, boolean exact, int matchLength) {
            this.node = node;
            this.exact = exact;
            this.matchLength = matchLength;
        }

        @Override
        public String toString() {
            return "NodeExact{" +
                    "node=" + node +
                    ", exact=" + exact +
                    ", matchLength=" + matchLength +
                    '}';
        }
    }
}
