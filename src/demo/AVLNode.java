package demo;

/**
 * Created by apple on 16/7/30.
 */
public class AVLNode {

    //�ڵ��ֵ
    public int key;

    //�ڵ��ƽ���
    public int balance;

    //�ֱ�ָ��ڵ�����ӡ��Һ����븸�ڵ�
    public AVLNode left, right, parent;

    /**
     * @function Ĭ�Ϲ��캯��
     * @param k
     * @param p
     */
    AVLNode(int k, AVLNode p) {
        key = k;
        parent = p;
    }
}