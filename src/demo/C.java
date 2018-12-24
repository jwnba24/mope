package demo;



import java.util.Scanner;
 
public class C {
	
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int len=sc.nextInt();
        //int[][] arr=new int[len][3];
        for(int i=0;i<len;i++){
        	String t=sc.next();
        	String[] p=new String[3];
        	p=t.split(" ");
        	System.out.println(p.length);
        	//System.out.println((int)(Math.ceil((float)arr[i][0]/(float)arr[i][2])*Math.ceil((float)arr[i][1]/(float)arr[i][2])));
        	
        	
        }
        
    }
}
