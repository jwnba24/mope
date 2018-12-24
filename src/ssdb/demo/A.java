package ssdb.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.insert.Insert;


import ssdb.core.Connector;
import ssdb.deparser.MOPE_InsertDeparser;

public class A {
	public static int number=5000;
	//每条执行时间
	public static long[] excuTimes=new long[number];
	//
	public static long[] calTimes=new long[number];
	//每条插入时间
	public static long[] insertTimes=new long[number];
	//每条的检查与调整时间
	public static long[] checkAndAdjTimes=new long[number];
	//
	public static int[] searchLength=new int[number];
	public static int index=0;
	public static void addSL(int len){
		searchLength[index++]=len;
	}
	
	public static void main(String[] args) throws Exception{
		Connection conn=Connector.openConnection();
		CCJSqlParserManager psm=new CCJSqlParserManager();
		List<String> sqls=new ArrayList<String>();
		List<String> sqls2=new ArrayList<String>();
		
		BufferedReader br=new BufferedReader(new FileReader("E:\\temp\\random\\random.txt"));
		String read;
		for(int i=0;i<number;i++){
			read=br.readLine();
			//sqls.add("INSERT INTO grades(score) VALUES("+read+")");
			sqls2.add("INSERT INTO grades2(score) VALUES("+read+")");
		}
//		for(int i=60021;i<60022;i++){
//			sqls2.add("INSERT INTO grades2(score) VALUES("+i+")");
//		}
		
		
		String filePath_2;
		String filePath_3;
		String filePath_4;
		File f;
		FileOutputStream o_2;
		FileOutputStream o_3;
		FileOutputStream o_4;
		int allInsert=0;
		int allAdj=0;
		int allCal=0;
		//		
//		System.out.println("*******************************MOPE:*******************************");
//		for(int i=0;i<sqls.size();i++){
//			Insert insert=(Insert)psm.parse(new StringReader(sqls.get(i)));
//			Times tmp=MOPE_InsertDeparser.handler(insert, conn);
//			excuTimes[i]=tmp.adjustTime+tmp.insertTime;
//			insertTimes[i]=tmp.insertTime;
//			checkAndAdjTimes[i]=tmp.adjustTime;
//			
//			System.out.print("@");
//			if((i+1)%100==0){
//				System.out.println("=="+(i+1));
//			}
//		}
//		
//		filePath_2="E:\\temp\\M_excuTimes.txt";
//		filePath_3="E:\\temp\\M_insertTimes.txt";
//		filePath_4="E:\\temp\\M_checkAndAdjTimes.txt";
//		f=new File(filePath_2);
//		if(!f.exists()){
//			f.createNewFile();
//		}
//		f=new File(filePath_3);
//		if(!f.exists()){
//			f.createNewFile();
//		}
//		f=new File(filePath_4);
//		if(!f.exists()){
//			f.createNewFile();
//		}
//		o_2=new FileOutputStream(filePath_2);
//		o_3=new FileOutputStream(filePath_3);
//		o_4=new FileOutputStream(filePath_4);
//		for(int i=0;i<sqls.size();i++){
//			o_2.write((excuTimes[i]+"\r\n").getBytes("GBK"));
//			o_3.write((insertTimes[i]+"\r\n").getBytes("GBK"));
//			o_4.write((checkAndAdjTimes[i]+"\r\n").getBytes("GBK"));
//		}
//		o_2.close();
//		o_3.close();
//		o_4.close();
//		
//		allInsert=0;
//		allAdj=0;
//		for(int i=0;i<sqls.size();i++){
//			allInsert+=insertTimes[i];
//			allAdj+=checkAndAdjTimes[i];
//		}
//		System.out.println("insert:"+allInsert+",adj:"+allAdj+",all:"+(allInsert+allAdj));
		
		System.out.println("*******************************CMOPE:*******************************");
		for(int i=0;i<sqls2.size();i++){
			Insert insert=(Insert)psm.parse(new StringReader(sqls2.get(i)));
			Times tmp=MOPE_InsertDeparser.handler(insert, conn);
			excuTimes[i]=tmp.adjustTime+tmp.insertTime+tmp.calTime;
			calTimes[i]=tmp.calTime;
			insertTimes[i]=tmp.insertTime;
			checkAndAdjTimes[i]=tmp.adjustTime;
			
			System.out.print("@");
			if((i+1)%100==0){
				System.out.println("=="+(i+1));
			}
		}
		
		
		filePath_2="E:\\temp\\CM_2_excuTimes.txt";
		filePath_3="E:\\temp\\CM_2_insertTimes.txt";
		filePath_4="E:\\temp\\CM_2_checkAndAdjTimes.txt";
		f=new File(filePath_2);
		if(!f.exists()){
			f.createNewFile();
		}
		f=new File(filePath_3);
		if(!f.exists()){
			f.createNewFile();
		}
		f=new File(filePath_4);
		if(!f.exists()){
			f.createNewFile();
		}
		o_2=new FileOutputStream(filePath_2);
		o_3=new FileOutputStream(filePath_3);
		o_4=new FileOutputStream(filePath_4);
		for(int i=0;i<sqls2.size();i++){
			o_2.write((excuTimes[i]+"\r\n").getBytes("GBK"));
			o_3.write((insertTimes[i]+"\r\n").getBytes("GBK"));
			o_4.write((checkAndAdjTimes[i]+"\r\n").getBytes("GBK"));
		}
		o_2.close();
		o_3.close();
		o_4.close();
		
		allCal=0;
		allInsert=0;
		allAdj=0;
		for(int i=0;i<sqls2.size();i++){
			allCal+=calTimes[i];
			allInsert+=insertTimes[i];
			allAdj+=checkAndAdjTimes[i];
		}
		System.out.println("cal:"+allCal+",insert:"+allInsert+",adj:"+allAdj+",all:"+(allCal+allInsert+allAdj));
	
//		filePath_2="E:\\temp\\CM_2_searchLength.txt";
//		f=new File(filePath_2);
//		if(!f.exists()){
//			f.createNewFile();
//		}
//		o_2=new FileOutputStream(filePath_2);
//		int alll=0;
//		for(int i=0;i<number;i++){
//			alll+=searchLength[i];
			//o_2.write((searchLength[i]+"\r\n").getBytes("GBK"));
//		}
//		System.out.println("all length:"+alll);
//		
	}
}
