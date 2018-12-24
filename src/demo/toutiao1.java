package demo;

import java.util.Scanner;

/**
 * Created by Administrator on 2018/4/15.
 */
public class toutiao1 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int N = scanner.nextInt();//表示数列个数
        int[][] array = new int[N][];
        for (int i = 0; i < N; i++) {
            int n = scanner.nextInt();//数列元素个数
            array[i] = new int[n + 1];
            array[i][0] = n;
            for (int j = 1; j < n + 1; j++) {
                array[i][j] = scanner.nextInt();
            }
        }

        for (int i = 0; i < N; i++) {
            int m = 1, n = array[i].length - 1;
            int maxT = array[i][array[i].length - 1] - array[i][1];
            int minT = maxT;
            for (int k = maxT; k > 0; k--) {
                while (array[i][n] + k > array[i][array[i].length - 1]) n--;
                while (array[i][m] - k < array[i][1]) m++;
                if (m > n) {
                    m = 1;
                    n = array[i].length - 1;
                    continue;
                } else {
                    boolean flag = true;
                    for (int j = m; j <= n; j++) {
                        flag = isTrue(array[i][j] + k, array[i]);
                        if (!flag) {
                            break;
                        }
                    }
                    if (flag) minT = k;
                }

            }
            System.out.println(minT);
        }
    }

    private static boolean isTrue(int x, int[] array) {
        boolean flag = false;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == x) {
                flag = true;
                break;
            }
        }
        return flag;
    }
}
