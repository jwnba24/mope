package jwnba24Test;

/**  
 * @author fjssharpsword  2016-7-20  
 * 实现一个二叉查找树的功能，可以进行动态插入、删除关键字；  
 * 查询给定关键字、最小关键字、最大关键字；转换为有序列表(用于排序)  
 */  
  
import java.util.ArrayList;  
import java.util.List;
import java.util.Random;

public class BinarySearchTree {
    // 树的根结点
    private static List<TreeNode> rootList = new ArrayList<>();
    // 遍历结点列表  
    private List<TreeNode> nodelist = new ArrayList<TreeNode>();  
    //定义树结构

  
    /** 
     * isEmpty: 判断二叉查找树是否为空；若为空，返回 true ，否则返回 false . 
     *  
     */  
    public boolean isEmpty() {  
        if (rootList == null) {
            return true;  
        } else {  
            return false;  
        }  
    }  
  
    /** 
     * TreeEmpty: 对于某些二叉查找树操作(比如删除关键字)来说，若树为空，则抛出异常。 
     */  
    public void TreeEmpty() throws Exception {  
        if (isEmpty()) {  
            throw new Exception("树为空!");  
        }
    }  

    /**
     * search: 在二叉查找树中查询给定关键字
     *
     * @param key 给定关键字
     * @return 匹配给定关键字的树结点
     */
    public TreeNode search(int key) {
        TreeNode pNode = rootList.get(0);
        while (pNode != null && pNode.key != key) {
            if (key < pNode.key) {
                pNode = pNode.leftChild;
            } else {
                pNode = pNode.rightChild;
            }
        }
        return pNode;
    }

    /**
     * minElemNode: 获取二叉查找树中的最小关键字结点
     *
     * @return 二叉查找树的最小关键字结点 ，一直向左
     * @throws Exception  若树为空，则抛出异常
     */
    public TreeNode minElemNode(TreeNode node) throws Exception {
        if (node == null) {
            throw new Exception("树为空！");
        }
        TreeNode pNode = node;
        while (pNode.leftChild != null) {
            pNode = pNode.leftChild;
        }
        return pNode;
    }

    /**
     * maxElemNode: 获取二叉查找树中的最大关键字结点
     *
     * @return 二叉查找树的最大关键字结点 ，一直向右
     * @throws Exception  若树为空，则抛出异常
     */
    public TreeNode maxElemNode(TreeNode node) throws Exception {
        final int a;
        if (node == null) {
            throw new Exception("树为空！");
        }
        TreeNode pNode = node;
        while (pNode.rightChild != null) {
            pNode = pNode.rightChild;
        }
        return pNode;
    }

    /**
     * successor: 获取给定结点在中序遍历顺序下的后继结点
     * @param node 给定树中的结点
     * @return 若该结点存在中序遍历顺序下的后继结点，则返回其后继结点；否则返回 null
     * @throws Exception
     */
    public TreeNode successor(TreeNode node) throws Exception {
        if (node == null) {
            return null;
        }

        // 若该结点的右子树不为空，则其后继结点就是右子树中的最小关键字结点
        if (node.rightChild != null) {
            return minElemNode(node.rightChild);
        }
        // 若该结点右子树为空
        TreeNode parentNode = node.parent;
        while (parentNode != null && node == parentNode.rightChild) {
            node = parentNode;
            parentNode = parentNode.parent;
        }
        return parentNode;
    }

    /**
     * precessor: 获取给定结点在中序遍历顺序下的前趋结点
     * @param node 给定树中的结点
     * @return 若该结点存在中序遍历顺序下的前趋结点，则返回其前趋结点；否则返回 null
     * @throws Exception
     */
    public TreeNode precessor(TreeNode node) throws Exception {
        if (node == null) {
            return null;
        }

        // 若该结点的左子树不为空，则其前趋结点就是左子树中的最大关键字结点
        if (node.leftChild != null) {
            return maxElemNode(node.leftChild);
        }
        // 若该结点左子树为空
        TreeNode parentNode = node.parent;
        while (parentNode != null && node == parentNode.leftChild) {
            node = parentNode;
            parentNode = parentNode.parent;
        }
        return parentNode;
    }

    /** 
     * insert: 将给定关键字插入到二叉查找树中 
     * 插入后要调整二叉查找树左小右大结构
     * @param key 给定关键字 
     */  
    public void insert(double key) {
        TreeNode parentNode = null;  
        TreeNode newNode = new TreeNode(key, null, null, null);
        TreeNode root=null;
        int addStr=1;
        if(rootList.size()!=0){
            BinarySearchTree bst = new BinarySearchTree();
            //如果森林中存在树，那么取出最后一棵树的根节点，然后遍历该树获取节点的个数
            TreeNode t=rootList.get(rootList.size()-1);
            int size=bst.inOrderTraverseList((rootList.size()-1)).size();
            addStr=rootList.size()+1;
            if(size<10000) root=t;

        }
        TreeNode pNode = root;
        if (root == null) {  
            root = newNode;
            root.setIndex(addStr+"");
            rootList.add(root);
            return;  
        }
        StringBuffer sb=new StringBuffer();
        while (pNode != null) {  
            parentNode = pNode;
            if (key < pNode.key) {  
                pNode = pNode.leftChild;  
            } else if (key > pNode.key) {  
                pNode = pNode.rightChild;  
            } else {  
                // 树中已存在匹配给定关键字的结点，则什么都不做直接返回  
                return;  
            }  
        }  
        if (key < parentNode.key) {  
            parentNode.leftChild = newNode;  
            newNode.parent = parentNode;
            newNode.setIndex(parentNode.getIndex()+sb.append("0").toString());
        } else {  
            parentNode.rightChild = newNode;  
            newNode.parent = parentNode;
            newNode.setIndex(parentNode.getIndex()+sb.append("1").toString());
        }
//        //用根节点的height表示整棵树的最大高度
//        if(newNode.getIndex().length()-1>root.getHeight()){
//            root.setHeight(newNode.getIndex().length()-1);
//        }
    }  

    /**
     * delete: 从二叉查找树中删除匹配给定关键字相应的树结点
     *
     * @param key  给定关键字
     */
    public void delete(int key) throws Exception {
        TreeNode pNode = search(key);
        if (pNode == null) {
            throw new Exception("树中不存在要删除的关键字!");
        }
        delete(pNode);
    }

    /** 
     * delete: 从二叉查找树中删除给定的结点. 
     *  
     * @param pNode  要删除的结点  前置条件： 给定结点在二叉查找树中已经存在 
     * 删除后要调整二叉查找树，满足左小右大结构
     * @throws Exception 
     */  
    private void delete(TreeNode pNode) throws Exception {  
        if (pNode == null) {  
            return;  
        }  
        if (pNode.leftChild == null && pNode.rightChild == null) { // 该结点既无左孩子结点，也无右孩子结点  
            TreeNode parentNode = pNode.parent;  
            if (pNode == parentNode.leftChild) {  
                parentNode.leftChild = null;  
            } else {  
                parentNode.rightChild = null;  
            }  
            pNode=null;
            return;  
        }  
        if (pNode.leftChild == null && pNode.rightChild != null) { // 该结点左孩子结点为空，右孩子结点非空  
            TreeNode parentNode = pNode.parent;  
            TreeNode rightNode=pNode.rightChild;
            if (pNode == parentNode.leftChild) {  
            	rightNode.parent = parentNode;  
                parentNode.leftChild = rightNode;  
            } else {  
            	rightNode.parent = parentNode;  
                parentNode.rightChild = rightNode;  
            }  
            pNode=null;
            return;  
        }  
        if (pNode.leftChild != null && pNode.rightChild == null) { // 该结点左孩子结点非空，右孩子结点为空  
            TreeNode parentNode = pNode.parent;  
            TreeNode leftNode=pNode.leftChild;
            if (pNode == parentNode.leftChild) {
            	leftNode.parent = parentNode;
                parentNode.leftChild = leftNode;            
            } else {  
            	leftNode.parent = parentNode;
                parentNode.rightChild = leftNode;                
            }  
            pNode=null;
            return;  
        }  
        if(pNode.leftChild != null && pNode.rightChild != null){// 该结点左右孩子结点均非空
        	TreeNode successorNode = successor(pNode);  
        	pNode.key = successorNode.key;  
        	delete(successorNode);  	
        }
    }  
  
    /** 
     * inOrderTraverseList: 获得二叉查找树的中序遍历结点列表 
     *  
     * @return 二叉查找树的中序遍历结点列表 
     */  
    public List<TreeNode> inOrderTraverseList(int i) {
        if (nodelist != null) {  
            nodelist.clear();  
        }  
        inOrderTraverse(rootList.get(i));
        return nodelist;  
    }  
  
    /** 
     * inOrderTraverse: 对给定二叉查找树进行中序遍历 
     *  
     * @param root 给定二叉查找树的根结点 
     */  
    private void inOrderTraverse(TreeNode root) {  
        if (root != null) {  
            inOrderTraverse(root.leftChild);  
            nodelist.add(root);  
            inOrderTraverse(root.rightChild);  
        }  
    }  
  
    /** 
     * toStringOfOrderList: 获取二叉查找树中关键字的有序列表 
     *  
     * @return 二叉查找树中关键字的有序列表 
     */  
    public String toStringOfOrderList(int i) {
        StringBuilder sbBuilder = new StringBuilder(" [ ");  
        for (TreeNode p : inOrderTraverseList( i)) {
            sbBuilder.append(p.key);  
            sbBuilder.append(" ");  
        }  
        sbBuilder.append("]");  
        return sbBuilder.toString();  
    }  
  
    /** 
     * 获取该二叉查找树的字符串表示 
     */  
    public String toString(int i) {
        StringBuilder sbBuilder = new StringBuilder(" [ ");  
        for (TreeNode p : inOrderTraverseList(i)) {
            sbBuilder.append(p.toString());  
            sbBuilder.append(" ");  
        }  
        sbBuilder.append("]");  
        return sbBuilder.toString();  
    }  
  
    public List<TreeNode> getRoot() {
        return rootList;
    }  

//    public static void testNode(BinarySearchTree bst, TreeNode pNode) throws Exception {
//        System.out.println("本结点: " + pNode);
//        System.out.println("前趋结点: " + bst.precessor(pNode));
//        System.out.println("后继结点: " + bst.successor(pNode));
//    }
//
//    public static void testTraverse(BinarySearchTree bst) {
//        System.out.println("二叉树遍历：" + bst.toString());
//        System.out.println("二叉查找树转换为有序列表: " + bst.toStringOfOrderList());
//    }
    public static void main(String[] args) {

        try {  
            BinarySearchTree bst = new BinarySearchTree();  
            //插入
//            int[] keys = new int[] { 52,18,69,32,10,2,7,72,86,98,100,5,1020,789,13,15,55,6,32,76,324,43,88,342,1,43,22,88,67,66 };
            Random random=new Random();
            for (int i=0;i<5000;i++) {
                bst.insert(random.nextInt(300000));
            }
            int count=0;
            for(int i=0;i<bst.rootList.size();i++){
                List<TreeNode> list=bst.inOrderTraverseList(i);
                System.out.println("当前二叉树编号为："+i+",大小为："+list.size());
                int max=0;
                for(TreeNode node:list){
                    if(node.getIndex().length()>max){
                        max=node.getIndex().length();
                    }
//                    System.out.println("节点的值："+node.getKey()+",节点的路径："+node.getIndex());
                }
                max--;
                double expect_height=Math.floor(Math.log(list.size())/Math.log(2))+1;
                System.out.println("期待二叉树高度："+expect_height+",实际二叉树的高度为"+max);
                boolean flag=expect_height>=max?true:false;
//                System.out.println("是否平衡："+flag);
                if(flag) count++;
            }
            System.out.println("平衡占比："+count);

        } catch (Exception e) {
            System.out.println(e.getMessage());  
            e.printStackTrace();  
        }  
    }

}  