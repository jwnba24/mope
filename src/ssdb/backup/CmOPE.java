package ssdb.backup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ssdb.core.NameHide;
import ssdb.core.mope.MOPE;

/**
 * cmope 工具类
 * 可自定义调整策略
 * @author zenggo
 *
 */
public class CmOPE {
	//参数P
	public static double Percent=0.5;
	public static void setPercent(double p){
		//P必需大于0小于1
		if(p>0&&p<1)
			Percent=p;
		else
			Percent=0.3;
	}
	
	public static boolean checkOPETree(String tableName,String colName,String insertPath,Connection conn) throws Exception{
		//boolean isRebalanced=false;
		//如果插入的是根节点，不回溯
		if(insertPath.length()==0){
			return false;
		}else{
			String secretColName=NameHide.getSecretName(colName);
			String check="CALL checkOPETree('"+tableName+"','"+colName+"','"+secretColName+"','"+insertPath+"',@irr)";
			Statement stmt=conn.createStatement();
			stmt.executeQuery(check);
			//获取是否发生了旋转操作
//			String qir="SELECT @irr";
//			ResultSet rs=stmt.executeQuery(qir);
//			rs.next();
//			int ird=rs.getInt("@irr");
//			if(ird==1) isRebalanced=true;
		}
		//return isRebalanced;
		return false;
	}
	
	
	
	
	
	//***************************************************************************************************************************************
	//************************************************以下为没搬到mysql上的代码，可删*********************************************************
	//***************************************************************************************************************************************
	/**
	 * 
	 * @param tableName
	 * @param colName
	 * @param insertPath
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public static boolean checkOPETree_1(String tableName,String colName,String insertPath,Connection conn) throws Exception{
		boolean isRebalanced=false;
		//计算密文列名
		String secretColName=NameHide.getSecretName(colName);
		String CipherIndexColName=secretColName+"_index";
		
		Statement stmt=conn.createStatement();
		//获取插入前OPE TREE的树高，与节点数量
		String q1="SELECT treeheight FROM metadata WHERE tablename = '"+tableName+"' AND columnname = '"+colName+"'";
		String q2="SELECT COUNT(DISTINCT("+CipherIndexColName+")) AS 'amount' FROM "+tableName;
		ResultSet rs=stmt.executeQuery(q1); 
		rs.next();
		int oldTreeHeight=rs.getInt("treeheight");
		rs=stmt.executeQuery(q2);
		rs.next();
		long nowNodesAmount=rs.getLong("amount");
		//获取本次插入路径长度+1为插入的树层次
		int nowInsertHeight=insertPath.length()+1;
		//获取插入后的树高
		int nowTreeHeight=nowInsertHeight>oldTreeHeight?nowInsertHeight:oldTreeHeight;
		//计算当前数量节点可构成的二叉树最小高度，即完全二叉树高度
		int leastTreeHeight=(int) (Math.floor(Math.log(nowNodesAmount)/Math.log(2))+1);
		
		//if(nowInsertHeight==MOPE.MOPE_LENGTH||nowNodesAmount<((long)Math.pow(2, nowTreeHeight)-1)*Percent){
		if(nowInsertHeight==MOPE.MOPE_LENGTH||nowInsertHeight>leastTreeHeight+4){
			isRebalanced=true;
			//触发调整操作
			reEncodeOPETree(tableName,colName,CipherIndexColName,nowNodesAmount,conn);
		}else{
			//若不触发调整且本次插入路径长度大于插入前树高，更新metadata中的树高
			if(nowTreeHeight>oldTreeHeight){
				String up1="UPDATE metadata SET treeheight = "+nowTreeHeight+" WHERE tablename = '"+tableName+"' AND columnname = '"+colName+"'";
				stmt.executeUpdate(up1);
			}
		}
		return isRebalanced;
	}
	/**
	 * 
	 * @param tableName
	 * @param colName
	 * @param CipherIndexColName
	 * @param amount
	 * @param conn
	 * @throws SQLException
	 */
	public static void reEncodeOPETree(String tableName,String colName,String CipherIndexColName,long amount,Connection conn) throws SQLException{
		//计算调整后的完全二叉树T*的高度
		int newTreeHeight=(int) (Math.floor(Math.log(amount)/Math.log(2))+1);
		//计算h-1层满二叉树节点数
		long upperAmount=(long) (Math.pow(2, newTreeHeight-1)-1);
		//计算T*中序号等于OPE编码的节点个数，即最后一个OPE编码等于序号的节点
		long last=2*(amount-upperAmount);
		//编码需补齐的长度
		int addLength=MOPE.MOPE_LENGTH-newTreeHeight;
		long addNum=(long) Math.pow(2, addLength);
		
		Statement stmt=conn.createStatement();
		stmt.addBatch("CREATE TEMPORARY TABLE tmp(index_old BIGINT,increment BIGINT NOT NULL AUTO_INCREMENT,PRIMARY KEY (increment),index_new BIGINT)");
		stmt.addBatch("INSERT INTO tmp(index_old) SELECT DISTINCT "+CipherIndexColName+" FROM "+tableName+" ORDER BY "+CipherIndexColName);
		stmt.addBatch("UPDATE tmp SET index_new = moveIndex(increment,"+last+")");
		stmt.addBatch("UPDATE tmp SET index_new = index_new*"+addNum);
		stmt.addBatch("UPDATE "+tableName+" A INNER JOIN tmp B ON A."+CipherIndexColName+" = B.index_old SET A."+CipherIndexColName+" = B.index_new");
		stmt.addBatch("DROP TABLE tmp");
		stmt.addBatch("UPDATE metadata SET treeheight = "+newTreeHeight+" WHERE tablename = '"+tableName+"' AND columnname = '"+colName+"'");
		stmt.executeBatch();
	}
}
