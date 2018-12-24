package ssdb.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import ssdb.core.Connector;
import ssdb.deparser.MOPE_InsertDeparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.insert.Insert;

public class Test {
	public static int adjustTimes=0;
	public static void addAT(){
		adjustTimes++;
	}
	public static void resetAT(){
		adjustTimes=0;
		for(int i=0;i<ATtimes.length;i++){
			ATtimes[i]=0;
		}
	}
	
	public static int[] ATtimes=new int[5];
	//每条执行时间
	public static long[] excuTimes=new long[5000];
	//每条插入时间
	public static long[] insertTimes=new long[5000];
	//每条的检查与调整时间
	public static long[] checkAndAdjTimes=new long[5000];
	//已插入条数-调整时间
	public static HashMap<Integer,Integer> aTimes=new HashMap();
	//
	public static boolean[] isRebalanced=new boolean[5000];
	
	public static void main(String[] args) throws Exception{
		Connection conn=Connector.openConnection();
		CCJSqlParserManager psm=new CCJSqlParserManager();
		resetAT();
		List<String> sqls=new ArrayList<String>();
		List<String> sqls2=new ArrayList<String>();
		
		String filePath="E:\\temp\\random\\random.txt";
		FileReader fr=new FileReader(filePath);
		BufferedReader br=new BufferedReader(fr);
		String read;
		for(int i=0;i<5000;i++){
			//int r=(int) Math.ceil(Math.random()*5000);
			//int r=i+1;
			read=br.readLine();
			sqls.add("INSERT INTO grades(score) VALUES("+read+")");
			sqls2.add("INSERT INTO grades2(score) VALUES("+read+")");
		}
		int j;
		System.out.println("*******************************MOPE:*******************************");
		resetAT();
		j=0;
		for(int i=0;i<sqls.size();i++){
			Insert insert=(Insert)psm.parse(new StringReader(sqls.get(i)));
			if(MOPE_InsertDeparser.handler(insert, conn)!=null){
				isRebalanced[i]=true;
				addAT();
			}else{
				isRebalanced[i]=false;
			}
			
			System.out.print("@");
			if((i+1)%100==0){
				System.out.println("=="+(i+1));
			}
			if(i+1==100||i+1==500||i+1==1000||i+1==2500||i+1==5000){
				ATtimes[j++]=adjustTimes;
			}
		}

		
		
		for(int i=0;i<ATtimes.length;i++){
			System.out.print("case:"+ATtimes[i]);
		}
		System.out.println();
		
//		System.out.println("*******************************CMOPE with index:*******************************");
//		resetAT();
//		j=0;
//		for(int i=0;i<sqls2.size();i++){
//			Insert insert=(Insert)psm.parse(new StringReader(sqls2.get(i)));
//			if(MOPE_InsertDeparser.handler(insert, conn)){
//				isRebalanced[i]=true;
//				addAT();
//			}else{
//				isRebalanced[i]=false;
//			}
//			System.out.print("@");
//			if((i+1)%100==0){
//				System.out.println("=="+(i+1));
//			}
//			if(i+1==100||i+1==500||i+1==1000||i+1==2500||i+1==5000){
//				ATtimes[j++]=adjustTimes;
//			}
//		}
//		for(int i=0;i<ATtimes.length;i++){
//			System.out.print("case:"+ATtimes[i]);
//		}
//		System.out.println();
		
		
		String filePath_2="E:\\temp\\M_adjlist.txt";
		File f=new File(filePath_2);
		if(!f.exists()){
			f.createNewFile();
		}
		FileOutputStream o_2=new FileOutputStream(filePath_2);
		for(int i=0,length=sqls.size();i<length;i++){
			if(isRebalanced[i]){
				o_2.write(("y\r\n").getBytes("GBK"));
			}else{
				o_2.write(("n\r\n").getBytes("GBK"));
			}
		}
		o_2.close();
	}
}
