package trashsoftware.duckSonTranslator.trees;

public abstract class Tree<T extends TreeNode<T, V>, V> {
    
    protected int size;  // how many values in this tree
    protected int nNodes;  // how many nodes in this tree
    
    public abstract void insert(String key, V value);
    
    public int size() {
        return size;
    }

    public int nNodes() {
        return nNodes;
    }
}
