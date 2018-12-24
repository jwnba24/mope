package jwnba24Test;

import java.util.Random;

/**
 * Created by Administrator on 2018/3/9.
 */
public class A {
    public static void main(String[] args) {
        Random random=new Random();
        for (int i=0;i<100;i++){
            System.out.println(random.nextInt(100));
        }

    }
}
