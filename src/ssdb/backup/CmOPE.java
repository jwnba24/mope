package ssdb.backup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ssdb.core.NameHide;
import ssdb.core.mope.MOPE;

/**
 * cmope ������
 * ���Զ����������
 * @author zenggo
 *
 */
public class CmOPE {
	//����P
	public static double Percent=0.5;
	public static void setPercent(double p){
		//P�������0С��1
		if(p>0&&p<1)
			Percent=p;
		else
			Percent=0.3;
	}
	
	public static boolean checkOPETree(String tableName,String colName,String insertPath,Connection conn) throws Exception{
		//boolean isRebalanced=false;
		//���������Ǹ��ڵ㣬������
		if(insertPath.length()==0){
			return false;
		}else{
			String secretColName=NameHide.getSecretName(colName);
			String check="CALL checkOPETree('"+tableName+"','"+colName+"','"+secretColName+"','"+insertPath+"',@irr)";
			Statement stmt=conn.createStatement();
			stmt.executeQuery(check);
			//��ȡ�Ƿ�������ת����
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
	//************************************************����Ϊû�ᵽmysql�ϵĴ��룬��ɾ*********************************************************
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
		//������������
		String secretColName=NameHide.getSecretName(colName);
		String CipherIndexColName=secretColName+"_index";
		
		Statement stmt=conn.createStatement();
		//��ȡ����ǰOPE TREE�����ߣ���ڵ�����
		String q1="SELECT treeheight FROM metadata WHERE tablename = '"+tableName+"' AND columnname = '"+colName+"'";
		String q2="SELECT COUNT(DISTINCT("+CipherIndexColName+")) AS 'amount' FROM "+tableName;
		ResultSet rs=stmt.executeQuery(q1); 
		rs.next();
		int oldTreeHeight=rs.getInt("treeheight");
		rs=stmt.executeQuery(q2);
		rs.next();
		long nowNodesAmount=rs.getLong("amount");
		//��ȡ���β���·������+1Ϊ����������
		int nowInsertHeight=insertPath.length()+1;
		//��ȡ����������
		int nowTreeHeight=nowInsertHeight>oldTreeHeight?nowInsertHeight:oldTreeHeight;
		//���㵱ǰ�����ڵ�ɹ��ɵĶ�������С�߶ȣ�����ȫ�������߶�
		int leastTreeHeight=(int) (Math.floor(Math.log(nowNodesAmount)/Math.log(2))+1);
		
		//if(nowInsertHeight==MOPE.MOPE_LENGTH||nowNodesAmount<((long)Math.pow(2, nowTreeHeight)-1)*Percent){
		if(nowInsertHeight==MOPE.MOPE_LENGTH||nowInsertHeight>leastTreeHeight+4){
			isRebalanced=true;
			//������������
			reEncodeOPETree(tableName,colName,CipherIndexColName,nowNodesAmount,conn);
		}else{
			//�������������ұ��β���·�����ȴ��ڲ���ǰ���ߣ�����metadata�е�����
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
		//������������ȫ������T*�ĸ߶�
		int newTreeHeight=(int) (Math.floor(Math.log(amount)/Math.log(2))+1);
		//����h-1�����������ڵ���
		long upperAmount=(long) (Math.pow(2, newTreeHeight-1)-1);
		//����T*����ŵ���OPE����Ľڵ�����������һ��OPE���������ŵĽڵ�
		long last=2*(amount-upperAmount);
		//�����貹��ĳ���
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
