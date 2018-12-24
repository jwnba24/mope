package demo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by apple on 16/7/30.
 */
public class AVLTree1 {

    //指向当前AVL树的根节点
    private AVLNode root;
//    private AVLNode[] treeList = new AVLNode[10];//用来存放多棵树的根节点
//    private int[] countNum=new int[10];

//    public int[] getCountNum() {
//        return countNum;
//    }

    /**
     * @param key
     * @return
     * @function 插入函数
     */
    int count = 0;

//    public AVLNode[] getTreeList() {
//        return treeList;
//    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private int influence = 2;

    public int getInfluence() {
        return influence;
    }

    public void setInfluence(int influence) {
        this.influence = influence;
    }

    public boolean insert(int key) {


        //如果当前根节点为空,则直接创建新节点
        if (root == null) {
            root = new AVLNode(key, null);
        } else {

            //设置新的临时节点
            AVLNode n = root;

            //指向当前的父节点
            AVLNode parent;

            //循环直至找到合适的插入位置
            while (true) {

                //如果查找到了相同值的节点
                if (n.key == key)

                    //则直接报错
                    return false;

                //将当前父节点指向当前节点
                parent = n;

                //判断是移动到左节点还是右节点
                boolean goLeft = n.key > key;
                n = goLeft ? n.left : n.right;

                //如果左孩子或者右孩子为空
                if (n == null) {
                    if (goLeft) {
                        //将节点挂载到左孩子上
                        parent.left = new AVLNode(key, parent);
                    } else {
                        //否则挂载到右孩子上
                        parent.right = new AVLNode(key, parent);
                    }

                    //重平衡该树

                    rebalance(parent);

                    break;
                }

                //如果不为空,则以n为当前节点进行查找
            }
        }
        return true;
    }

    /**
     * @param delKey
     * @function 根据关键值删除某个元素, 需要对树进行再平衡
     */
    public void delete(int delKey) {

        if (root == null)
            return;
        AVLNode n = root;
        AVLNode parent = root;
        AVLNode delAVLNode = null;
        AVLNode child = root;

        while (child != null) {
            parent = n;
            n = child;
            child = delKey >= n.key ? n.right : n.left;
            if (delKey == n.key)
                delAVLNode = n;
        }

        if (delAVLNode != null) {
            delAVLNode.key = n.key;

            child = n.left != null ? n.left : n.right;

            if (root.key == delKey) {
                root = child;
            } else {
                if (parent.left == n) {
                    parent.left = child;
                } else {
                    parent.right = child;
                }
                rebalance(parent);
            }
        }
    }

    /**
     * @function 打印节点的平衡度
     */
//    public void printBalance() {
//        printBalance(root);
//    }

    /**
     * @param n
     * @function 重平衡该树
     */
    private void rebalance(AVLNode n) {

        //为每个节点设置相对高度
        setBalance(n);

        //如果左子树高于右子树
        if (n.balance == -influence) {
            count++;
            //如果挂载的是左子树的左孩子
            if (height(n.left.left) >= height(n.left.right))

                //进行右旋操作
                n = rotateRight(n);
            else

                //如果挂载的是左子树的右孩子,则先左旋后右旋
                n = rotateLeftThenRight(n);

        }
        //如果左子树高于右子树
        else if (n.balance == influence) {
            count++;
            //如果挂载的是右子树的右孩子
            if (height(n.right.right) >= height(n.right.left))
                //进行左旋操作
                n = rotateLeft(n);
            else
                //否则进行先右旋后左旋
                n = rotateRightThenLeft(n);
        }

        if (n.parent != null) {
            //如果当前节点的父节点不为空,则平衡其父节点
            rebalance(n.parent);
        } else {
            root = n;
        }
    }

    /**
     * @param a
     * @return
     * @function 左旋操作
     */
    private AVLNode rotateLeft(AVLNode a) {

        //指向当前节点的右孩子
        AVLNode b = a.right;

        //将当前节点的右孩子挂载到当前节点的父节点
        b.parent = a.parent;

        //将原本节点的右孩子挂载到新节点的左孩子
        a.right = b.left;

        if (a.right != null)
            a.right.parent = a;

        //将原本节点挂载到新节点的左孩子上
        b.left = a;

        //将原本节点的父节点设置为新节点
        a.parent = b;

        //如果当前节点的父节点不为空
        if (b.parent != null) {
            if (b.parent.right == a) {
                b.parent.right = b;
            } else {
                b.parent.left = b;
            }
        }

        //重新计算每个节点的平衡度
        setBalance(a, b);

        return b;
    }

    private AVLNode rotateRight(AVLNode a) {

        AVLNode b = a.left;
        b.parent = a.parent;

        a.left = b.right;

        if (a.left != null)
            a.left.parent = a;

        b.right = a;
        a.parent = b;

        if (b.parent != null) {
            if (b.parent.right == a) {
                b.parent.right = b;
            } else {
                b.parent.left = b;
            }
        }

        setBalance(a, b);

        return b;
    }

    private AVLNode rotateLeftThenRight(AVLNode n) {
        n.left = rotateLeft(n.left);
        return rotateRight(n);
    }

    private AVLNode rotateRightThenLeft(AVLNode n) {
        n.right = rotateRight(n.right);
        return rotateLeft(n);
    }

    /**
     * @param n
     * @return
     * @function 计算某个节点的高度
     */
    private int height(AVLNode n) {
        if (n == null)
            return -1;
        return 1 + Math.max(height(n.left), height(n.right));
    }

    /**
     * @param AVLNodes
     * @function 重设置每个节点的平衡度
     */
    private void setBalance(AVLNode... AVLNodes) {
        for (AVLNode n : AVLNodes)
            n.balance = height(n.right) - height(n.left);
    }

    private void printBalance(AVLNode n) {
        if (n != null) {
            printBalance(n.left);
            System.out.printf("%s ", n.balance);
            printBalance(n.right);
        }
    }

    private void inOrderTraverse(AVLNode root,ArrayList<AVLNode> nodeList) {
        if (root != null) {
            inOrderTraverse(root.left,nodeList);
            nodeList.add(root);
            inOrderTraverse(root.right,nodeList);
        }
    }

    public AVLNode search(int key) {
        int i=0;
        AVLNode node = root;
        while (node != null && node.key != key) {
            i++;
            if (key < node.key) {
                node = node.left;
            } else {
                node = node.right;
            }
        }
        System.out.println("一共查找次数" + i);
        return node;
    }
    private int sumNode(AVLNode node){
        if(node==null){
            return 0;
        }else{
            int a=sumNode(node.left);
            int b=sumNode(node.right);
            return 1+a+b;
        }
    }
    private void levelRead(AVLNode root)
    {
        if(root == null) return;
        Queue<AVLNode> queue = new LinkedList<AVLNode>() ;
        queue.add(root);
        int count=1;
        while(queue.size() != 0)
        {
            int len = queue.size();
            System.out.println("第"+count+"层");
            StringBuffer sb=new StringBuffer();
            for(int i=0;i <len; i++)
            {

                AVLNode temp = queue.poll();
//                System.out.print(temp.key+" ");
                sb.append(temp.key+" ");
                if(temp.left != null)  queue.add(temp.left);
                if(temp.right != null) queue.add(temp.right);
            }
            System.out.println(sb.toString());
            count++;
        }
    }

    public static void main(String[] args) {
        Random random = new Random();
        AVLTree1 avlTree = new AVLTree1();
//        for(int k=0;k<10;k++){
//            avlTree.setInfluence(4);
//            avlTree.setCount(0);
//            long start=System.currentTimeMillis();
//            for(int i=0;i<500+k*500;i++){
//                avlTree.insert(random.nextInt(500000));
//            }
//            long end=System.currentTimeMillis();
//            System.out.println("重排因子为"+k+",重排序次数:"+avlTree.getCount()+",总执行时间:"+(end-start));
//        }
        avlTree.setInfluence(2);

        int max = 10000;
        int min = 1000;
        long start=System.currentTimeMillis();
        for (int i = 0; i < 5000; i++) {
//            avlTree.insert(random.nextInt(max) % (max - min + 1) + min);
            avlTree.insert(i);
        }
        long end=System.currentTimeMillis();
        System.out.println("执行时间："+(end-start));
        System.out.println(avlTree.height(avlTree.root)+":"+avlTree.sumNode(avlTree.root));
//        avlTree.levelRead(avlTree.root);
        avlTree.search(0);

    }
}