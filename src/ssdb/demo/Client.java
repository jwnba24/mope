package ssdb.demo;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ssdb.core.Connector;
import ssdb.deparser.MOPE_InsertDeparser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.insert.Insert;

public class Client {
	public static void main(String[] args) throws Exception{
		Connection conn=Connector.openConnection();
		CCJSqlParserManager psm=new CCJSqlParserManager();
		Scanner sc = new Scanner(System.in);
//		System.out.println("«Î ‰»ÎSQL”Ôæ‰£∫");
//		String inputSQL = sc.nextLine();
//		while(!inputSQL.equals("exit")){
//			Insert insert=(Insert)psm.parse(new StringReader(inputSQL));
//			MOPE_InsertDeparser.handler(insert, conn);
//			inputSQL = sc.nextLine();
//		}
		List<String> sqls=new ArrayList<String>();
//		sqls.add("INSERT INTO students(name,age,salary,sex) VALUES ('zrk',15,13,'male')");
//		sqls.add("INSERT INTO students(name,age,salary,sex) VALUES ('alex',8,6,'female')");
//		sqls.add("INSERT INTO students(name,age,salary,sex) VALUES ('arsenal',4,15,'male')");
//		sqls.add("INSERT INTO students(name,age,salary,sex) VALUES ('Akun',2,4,'male')");
//		sqls.add("INSERT INTO students(name,age,salary,sex) VALUES ('monreal',6,8,'male')");
//		sqls.add("INSERT INTO students(name,age,salary,sex) VALUES ('eva',5,2,'female')");
//		sqls.add("INSERT INTO students(name,age,salary,sex) VALUES ('Akun',18,2,'male')");
//		sqls.add("INSERT INTO students(name,age,salary,sex) VALUES ('Akun',9,2,'male')");
//		sqls.add("INSERT INTO students(name,age,salary,sex) VALUES ('Akun',7,2,'male')");
//		sqls.add("INSERT INTO students(name,age,salary,sex) VALUES ('Akun',10,2,'male')");
//		sqls.add("INSERT INTO students(name,age,salary,sex) VALUES ('kyo',6.5,3,'male')");
//		sqls.add("INSERT INTO students(name,age,salary,sex) VALUES ('nerd',7.5,2,'male')");
		sqls.add("INSERT INTO grades2(score) VALUES(21)");
		sqls.add("INSERT INTO grades2(score) VALUES(20)");
		sqls.add("INSERT INTO grades2(score) VALUES(25)");
		sqls.add("INSERT INTO grades2(score) VALUES(17)");
		sqls.add("INSERT INTO grades2(score) VALUES(23)");
		sqls.add("INSERT INTO grades2(score) VALUES(29)");
		sqls.add("INSERT INTO grades2(score) VALUES(13)");
		sqls.add("INSERT INTO grades2(score) VALUES(10)");
		sqls.add("INSERT INTO grades2(score) VALUES(13)");
		sqls.add("INSERT INTO grades2(score) VALUES(10)");
		sqls.add("INSERT INTO grades2(score) VALUES(21)");
//		sqls.add("INSERT INTO grades_copy(score) VALUES(21)");
//		sqls.add("INSERT INTO grades_copy(score) VALUES(20)");
//		sqls.add("INSERT INTO grades_copy(score) VALUES(25)");
//		sqls.add("INSERT INTO grades_copy(score) VALUES(17)");
//		sqls.add("INSERT INTO grades_copy(score) VALUES(23)");
//		sqls.add("INSERT INTO grades_copy(score) VALUES(29)");
//		sqls.add("INSERT INTO grades_copy(score) VALUES(13)");
//		sqls.add("INSERT INTO grades_copy(score) VALUES(10)");
//		sqls.add("INSERT INTO grades_copy(score) VALUES(13)");
//		sqls.add("INSERT INTO grades_copy(score) VALUES(10)");
//		sqls.add("INSERT INTO grades_copy(score) VALUES(21)");
		//long allstart = System.currentTimeMillis(); 
		for(int i=0;i<sqls.size();i++){
			//long start = System.currentTimeMillis(); 
			Insert insert=(Insert)psm.parse(new StringReader(sqls.get(i)));
			MOPE_InsertDeparser.handler(insert, conn);
			//long end = System.currentTimeMillis();
			//System.out.println(end-start);
			//sc.nextLine();
		}
		//long allend = System.currentTimeMillis();
		//System.out.println(allend-allstart);
	}
}
