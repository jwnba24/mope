package jwnba24Test;

public class TreeNode {

    public double key;
    public TreeNode leftChild;
    public TreeNode rightChild;
    public TreeNode parent;
    public String index;
    public int height;


    public TreeNode(double key, TreeNode leftChild, TreeNode rightChild, TreeNode parent) {
        this.key = key;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.parent = parent;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getIndex() {
        return index;
    }

    public double getKey() {
        return key;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public String toString() {
        String leftkey = (leftChild == null ? "" : String.valueOf(leftChild.key));
        String rightkey = (rightChild == null ? "" : String.valueOf(rightChild.key));
        return "(" + leftkey + " , " + key + " , " + rightkey + ")";
    }
}