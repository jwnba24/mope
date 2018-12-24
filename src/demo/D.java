package demo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class D {
	public static void main(String[] args) throws IOException{
		int all=0;
		int[] array=new int[10];
		int j=0;
		BufferedReader br=new BufferedReader(new FileReader("E:\\temp\\cmope_2\\CM_2_checkAndAdjTimes.txt"));
		BufferedReader br_2=new BufferedReader(new FileReader("E:\\temp\\cmope_2\\CM_2_adjlist.txt"));
		String read;
		String read_2;
		for(int i=1;i<=5000;i++){
			read=br.readLine();
			read_2=br_2.readLine();
			if(read_2.equals("y")){
				all+=Integer.parseInt(read);
			}
			//all+=Integer.parseInt(read);
//			if(i%500==0){
//				array[j++]=all;
//				all=0;
//			}
		}
//		for(int i=0;i<array.length;i++){
//			//System.out.print((int)(Math.ceil(array[i]/1000))+",");
//			System.out.print(array[i]+",");
//		}
		System.out.println(all);
	}
}
