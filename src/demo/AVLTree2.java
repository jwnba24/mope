package demo;

import jwnba24Test.TreeNode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Created by apple on 16/7/30.
 */
public class AVLTree2 {

    //ָ��ǰAVL���ĸ��ڵ�
//    private AVLNode root;
    private AVLNode[] treeList = new AVLNode[5000];//������Ŷ�����ĸ��ڵ�
    private int[] countNum=new int[5000];
    long createTreeTime=0;

    public int[] getCountNum() {
        return countNum;
    }

    /**
     * @param key
     * @return
     * @function ���뺯��
     */
    int count = 0;

    public AVLNode[] getTreeList() {
        return treeList;
    }

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
        //���ȸ���keyֵģ1000������̣�������ֵȷ�����������ڵ�
        int index = key / 1000;//�����������ڸ�

        AVLNode root = null;
//        if (index < 1) return false;
        root = treeList[index];
        //�����ǰ���ڵ�Ϊ��,��ֱ�Ӵ����½ڵ�
        if (root == null) {
            root = new AVLNode(key, null);
            treeList[index] = root;
        } else {

            //�����µ���ʱ�ڵ�
            AVLNode n = root;

            //ָ��ǰ�ĸ��ڵ�
            AVLNode parent;

            //ѭ��ֱ���ҵ����ʵĲ���λ��
            while (true) {

                //������ҵ�����ֵͬ�Ľڵ�
                if (n.key == key)

                    //��ֱ�ӱ���
                    return false;

                //����ǰ���ڵ�ָ��ǰ�ڵ�
                parent = n;

                //�ж����ƶ�����ڵ㻹���ҽڵ�
                boolean goLeft = n.key > key;
                n = goLeft ? n.left : n.right;

                //������ӻ����Һ���Ϊ��
                if (n == null) {
                    if (goLeft) {
                        //���ڵ���ص�������
                        parent.left = new AVLNode(key, parent);
                    } else {
                        //������ص��Һ�����
                        parent.right = new AVLNode(key, parent);
                    }

                    //��ƽ�����

                    rebalance(root, index);

                    break;
                }

                //�����Ϊ��,����nΪ��ǰ�ڵ���в���
            }
        }
        return true;
    }

    /**
     * @param delKey
     * @function ���ݹؼ�ֵɾ��ĳ��Ԫ��, ��Ҫ����������ƽ��
     */
    public void delete(int delKey) {
        int index = delKey / 1000;
        AVLNode root = null;
        root = treeList[index];
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
                rebalance(parent, index);
            }
        }
    }

    /**
     * @function ��ӡ�ڵ��ƽ���
     */
//    public void printBalance() {
//        printBalance(root);
//    }

    /**
     * @param n
     * @function ��ƽ�����
     */
    private void rebalance(AVLNode n, int index) {

        //Ϊÿ���ڵ�������Ը߶�
        setBalance(n);
        ArrayList<Integer> list=null;
        //�������������������
        if (n.balance == -influence) {
            countNum[index]++;
            //������ص���������������
            list=new ArrayList<>();
            inOrderTraverse(n,list);
            //����һ���µĶ�����
            AVLNode node=createTree(list);
            treeList[index]=node;
        }

        //�������������������
        else if (n.balance == influence) {
            countNum[index]++;
            //������ص������������Һ���
            list=new ArrayList<>();
            inOrderTraverse(n,list);
            //����һ���µĶ�����
            long start=System.currentTimeMillis();
            AVLNode node=createTree(list);
            long end=System.currentTimeMillis();
            createTreeTime+=(end-start);
            treeList[index]=node;
        }
        //�����µĶ�����

    }

    private AVLNode createTree(ArrayList<Integer> list) {
        if(list==null||list.size()==0) return null;
        AVLNode node=null;
        if(list.size()==1) {
            node=new AVLNode(list.get(0),null);
            return node;
        }
        int size=list.size();
        int index=size/2;
        node=new AVLNode(list.get(index),null);
        ArrayList<Integer> leftList=new ArrayList<>();
        for(int i=0;i<index;i++){
            leftList.add(list.get(i));
        }
        ArrayList<Integer> rightList=new ArrayList<>();
        for(int i=index+1;i<size;i++){
            rightList.add(list.get(i));
        }
        node.left=createTree( leftList);
        node.right=createTree(rightList);
//        System.out.println("zhixing");
        return node;
    }

    /**
     * @param a
     * @return
     * @function ��������
     */
    private AVLNode rotateLeft(AVLNode a) {

        //ָ��ǰ�ڵ���Һ���
        AVLNode b = a.right;

        //����ǰ�ڵ���Һ��ӹ��ص���ǰ�ڵ�ĸ��ڵ�
        b.parent = a.parent;

        //��ԭ���ڵ���Һ��ӹ��ص��½ڵ������
        a.right = b.left;

        if (a.right != null)
            a.right.parent = a;

        //��ԭ���ڵ���ص��½ڵ��������
        b.left = a;

        //��ԭ���ڵ�ĸ��ڵ�����Ϊ�½ڵ�
        a.parent = b;

        //�����ǰ�ڵ�ĸ��ڵ㲻Ϊ��
        if (b.parent != null) {
            if (b.parent.right == a) {
                b.parent.right = b;
            } else {
                b.parent.left = b;
            }
        }

        //���¼���ÿ���ڵ��ƽ���
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
     * @function ����ĳ���ڵ�ĸ߶�
     */
    private int height(AVLNode n) {
        if (n == null)
            return -1;
        return 1 + Math.max(height(n.left), height(n.right));
    }

    /**
     * @param AVLNodes
     * @function ������ÿ���ڵ��ƽ���
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

    private void inOrderTraverse(AVLNode root,ArrayList<Integer> nodeList) {
        if (root != null) {
            inOrderTraverse(root.left,nodeList);
            nodeList.add(root.key);
            inOrderTraverse(root.right,nodeList);
        }
    }
    private void inOrderTraverse1(AVLNode root,ArrayList<AVLNode> nodeList) {
        if (root != null) {
            inOrderTraverse1(root.left,nodeList);
            nodeList.add(root);
            inOrderTraverse1(root.right,nodeList);
        }
    }

    public AVLNode search(int key) throws Exception{

        SuNumerGenerate.encrypt(new BigInteger("2"));

        int count = 0;
        int index = key / 1000;
        if (index == 0) index=1;
        AVLNode root = treeList[index - 1];
        AVLNode node = root;
        while (node != null && node.key != key) {
            count++;
            if (key < node.key) {
                node = node.left;
            } else {
                node = node.right;
            }
        }
        Thread.sleep(count*20);
        System.out.println("һ�����Ҵ���" + count);
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
            System.out.println("��"+count+"��");
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
        AVLTree2 avlTree = new AVLTree2();
//        for(int k=0;k<10;k++){
//            avlTree.setInfluence(4);
//            avlTree.setCount(0);
//            long start=System.currentTimeMillis();
//            for(int i=0;i<500+k*500;i++){
//                avlTree.insert(random.nextInt(500000));
//            }
//            long end=System.currentTimeMillis();
//            System.out.println("��������Ϊ"+k+",���������:"+avlTree.getCount()+",��ִ��ʱ��:"+(end-start));
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
        System.out.println("ִ��ʱ�䣺"+(end-start));
        start = System.currentTimeMillis();
        try {
            avlTree.search(99);
        } catch (Exception e) {
            e.printStackTrace();
        }
        end = System.currentTimeMillis();
        System.out.println("search time:"+(end-start));
//        System.out.println("��ƽ�������"+avlTree.count);
//        AVLNode[] nodeArray=avlTree.getTreeList();
//        for(int i=0;i<10;i++){
//            System.out.println(avlTree.height(nodeArray[i])+":"+avlTree.sumNode(nodeArray[i]));
//        }

//        AVLNode test=avlTree.getTreeList()[0];
//        System.out.println(avlTree.height(test)+":"+avlTree.sumNode(test));
//        avlTree.levelRead(test);

    }
}