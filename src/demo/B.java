package demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ssdb.core.Connector;
import ssdb.core.mope.MOPE;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BitsValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.insert.Insert;

public class B {
	public static void main(String[] args) throws JSQLParserException, SQLException, IOException{
		String filePath="E:\\temp\\random2.txt";
		File f=new File(filePath);
		if(!f.exists()){
			f.createNewFile();
		}
		FileOutputStream o=new FileOutputStream(filePath);
		for(int i=0;i<100000;i++){
			int r=(int) Math.ceil(Math.random()*5000000);
			o.write((r+"\r\n").getBytes("GBK"));
		}
		o.close();
//		FileReader fr=new FileReader(filePath);
//		BufferedReader br=new BufferedReader(fr);
//		String read;
//		while((read=br.readLine())!=null){
//			System.out.println(read);
//		}
	}
}
