package jwnba24Test;

/**  
 * @author fjssharpsword  2016-7-20  
 * ʵ��һ������������Ĺ��ܣ����Խ��ж�̬���롢ɾ���ؼ��֣�  
 * ��ѯ�����ؼ��֡���С�ؼ��֡����ؼ��֣�ת��Ϊ�����б�(��������)  
 */  
  
import java.util.ArrayList;  
import java.util.List;
import java.util.Random;

public class BinarySearchTree {
    // ���ĸ����
    private static List<TreeNode> rootList = new ArrayList<>();
    // ��������б�  
    private List<TreeNode> nodelist = new ArrayList<TreeNode>();  
    //�������ṹ

  
    /** 
     * isEmpty: �ж϶���������Ƿ�Ϊ�գ���Ϊ�գ����� true �����򷵻� false . 
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
     * TreeEmpty: ����ĳЩ�������������(����ɾ���ؼ���)��˵������Ϊ�գ����׳��쳣�� 
     */  
    public void TreeEmpty() throws Exception {  
        if (isEmpty()) {  
            throw new Exception("��Ϊ��!");  
        }
    }  

    /**
     * search: �ڶ���������в�ѯ�����ؼ���
     *
     * @param key �����ؼ���
     * @return ƥ������ؼ��ֵ������
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
     * minElemNode: ��ȡ����������е���С�ؼ��ֽ��
     *
     * @return �������������С�ؼ��ֽ�� ��һֱ����
     * @throws Exception  ����Ϊ�գ����׳��쳣
     */
    public TreeNode minElemNode(TreeNode node) throws Exception {
        if (node == null) {
            throw new Exception("��Ϊ�գ�");
        }
        TreeNode pNode = node;
        while (pNode.leftChild != null) {
            pNode = pNode.leftChild;
        }
        return pNode;
    }

    /**
     * maxElemNode: ��ȡ����������е����ؼ��ֽ��
     *
     * @return ��������������ؼ��ֽ�� ��һֱ����
     * @throws Exception  ����Ϊ�գ����׳��쳣
     */
    public TreeNode maxElemNode(TreeNode node) throws Exception {
        final int a;
        if (node == null) {
            throw new Exception("��Ϊ�գ�");
        }
        TreeNode pNode = node;
        while (pNode.rightChild != null) {
            pNode = pNode.rightChild;
        }
        return pNode;
    }

    /**
     * successor: ��ȡ����������������˳���µĺ�̽��
     * @param node �������еĽ��
     * @return ���ý������������˳���µĺ�̽�㣬�򷵻����̽�㣻���򷵻� null
     * @throws Exception
     */
    public TreeNode successor(TreeNode node) throws Exception {
        if (node == null) {
            return null;
        }

        // ���ý�����������Ϊ�գ������̽������������е���С�ؼ��ֽ��
        if (node.rightChild != null) {
            return minElemNode(node.rightChild);
        }
        // ���ý��������Ϊ��
        TreeNode parentNode = node.parent;
        while (parentNode != null && node == parentNode.rightChild) {
            node = parentNode;
            parentNode = parentNode.parent;
        }
        return parentNode;
    }

    /**
     * precessor: ��ȡ����������������˳���µ�ǰ�����
     * @param node �������еĽ��
     * @return ���ý������������˳���µ�ǰ����㣬�򷵻���ǰ����㣻���򷵻� null
     * @throws Exception
     */
    public TreeNode precessor(TreeNode node) throws Exception {
        if (node == null) {
            return null;
        }

        // ���ý�����������Ϊ�գ�����ǰ���������������е����ؼ��ֽ��
        if (node.leftChild != null) {
            return maxElemNode(node.leftChild);
        }
        // ���ý��������Ϊ��
        TreeNode parentNode = node.parent;
        while (parentNode != null && node == parentNode.leftChild) {
            node = parentNode;
            parentNode = parentNode.parent;
        }
        return parentNode;
    }

    /** 
     * insert: �������ؼ��ֲ��뵽����������� 
     * �����Ҫ���������������С�Ҵ�ṹ
     * @param key �����ؼ��� 
     */  
    public void insert(double key) {
        TreeNode parentNode = null;  
        TreeNode newNode = new TreeNode(key, null, null, null);
        TreeNode root=null;
        int addStr=1;
        if(rootList.size()!=0){
            BinarySearchTree bst = new BinarySearchTree();
            //���ɭ���д���������ôȡ�����һ�����ĸ��ڵ㣬Ȼ�����������ȡ�ڵ�ĸ���
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
                // �����Ѵ���ƥ������ؼ��ֵĽ�㣬��ʲô������ֱ�ӷ���  
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
//        //�ø��ڵ��height��ʾ�����������߶�
//        if(newNode.getIndex().length()-1>root.getHeight()){
//            root.setHeight(newNode.getIndex().length()-1);
//        }
    }  

    /**
     * delete: �Ӷ����������ɾ��ƥ������ؼ�����Ӧ�������
     *
     * @param key  �����ؼ���
     */
    public void delete(int key) throws Exception {
        TreeNode pNode = search(key);
        if (pNode == null) {
            throw new Exception("���в�����Ҫɾ���Ĺؼ���!");
        }
        delete(pNode);
    }

    /** 
     * delete: �Ӷ����������ɾ�������Ľ��. 
     *  
     * @param pNode  Ҫɾ���Ľ��  ǰ�������� ��������ڶ�����������Ѿ����� 
     * ɾ����Ҫ���������������������С�Ҵ�ṹ
     * @throws Exception 
     */  
    private void delete(TreeNode pNode) throws Exception {  
        if (pNode == null) {  
            return;  
        }  
        if (pNode.leftChild == null && pNode.rightChild == null) { // �ý��������ӽ�㣬Ҳ���Һ��ӽ��  
            TreeNode parentNode = pNode.parent;  
            if (pNode == parentNode.leftChild) {  
                parentNode.leftChild = null;  
            } else {  
                parentNode.rightChild = null;  
            }  
            pNode=null;
            return;  
        }  
        if (pNode.leftChild == null && pNode.rightChild != null) { // �ý�����ӽ��Ϊ�գ��Һ��ӽ��ǿ�  
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
        if (pNode.leftChild != null && pNode.rightChild == null) { // �ý�����ӽ��ǿգ��Һ��ӽ��Ϊ��  
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
        if(pNode.leftChild != null && pNode.rightChild != null){// �ý�����Һ��ӽ����ǿ�
        	TreeNode successorNode = successor(pNode);  
        	pNode.key = successorNode.key;  
        	delete(successorNode);  	
        }
    }  
  
    /** 
     * inOrderTraverseList: ��ö���������������������б� 
     *  
     * @return ����������������������б� 
     */  
    public List<TreeNode> inOrderTraverseList(int i) {
        if (nodelist != null) {  
            nodelist.clear();  
        }  
        inOrderTraverse(rootList.get(i));
        return nodelist;  
    }  
  
    /** 
     * inOrderTraverse: �Ը����������������������� 
     *  
     * @param root ��������������ĸ���� 
     */  
    private void inOrderTraverse(TreeNode root) {  
        if (root != null) {  
            inOrderTraverse(root.leftChild);  
            nodelist.add(root);  
            inOrderTraverse(root.rightChild);  
        }  
    }  
  
    /** 
     * toStringOfOrderList: ��ȡ����������йؼ��ֵ������б� 
     *  
     * @return ����������йؼ��ֵ������б� 
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
     * ��ȡ�ö�����������ַ�����ʾ 
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
//        System.out.println("�����: " + pNode);
//        System.out.println("ǰ�����: " + bst.precessor(pNode));
//        System.out.println("��̽��: " + bst.successor(pNode));
//    }
//
//    public static void testTraverse(BinarySearchTree bst) {
//        System.out.println("������������" + bst.toString());
//        System.out.println("���������ת��Ϊ�����б�: " + bst.toStringOfOrderList());
//    }
    public static void main(String[] args) {

        try {  
            BinarySearchTree bst = new BinarySearchTree();  
            //����
//            int[] keys = new int[] { 52,18,69,32,10,2,7,72,86,98,100,5,1020,789,13,15,55,6,32,76,324,43,88,342,1,43,22,88,67,66 };
            Random random=new Random();
            for (int i=0;i<5000;i++) {
                bst.insert(random.nextInt(300000));
            }
            int count=0;
            for(int i=0;i<bst.rootList.size();i++){
                List<TreeNode> list=bst.inOrderTraverseList(i);
                System.out.println("��ǰ���������Ϊ��"+i+",��СΪ��"+list.size());
                int max=0;
                for(TreeNode node:list){
                    if(node.getIndex().length()>max){
                        max=node.getIndex().length();
                    }
//                    System.out.println("�ڵ��ֵ��"+node.getKey()+",�ڵ��·����"+node.getIndex());
                }
                max--;
                double expect_height=Math.floor(Math.log(list.size())/Math.log(2))+1;
                System.out.println("�ڴ��������߶ȣ�"+expect_height+",ʵ�ʶ������ĸ߶�Ϊ"+max);
                boolean flag=expect_height>=max?true:false;
//                System.out.println("�Ƿ�ƽ�⣺"+flag);
                if(flag) count++;
            }
            System.out.println("ƽ��ռ�ȣ�"+count);

        } catch (Exception e) {
            System.out.println(e.getMessage());  
            e.printStackTrace();  
        }  
    }

}  