package ssdb.core.mope;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ssdb.core.NameHide;

/**
 * cmope ������
 * ���Զ����������
 * @author zenggo
 *
 */
public class CmOPE {
	//����P
	public static double Percent=0.5;
	
	public static boolean checkOPETree(String tableName,String colName,String insertPath,Connection conn) throws Exception{
		//boolean isRebalanced=false;
		//���������Ǹ��ڵ㣬������
		if(insertPath.length()==0){
			return false;
		}else{
			String check="CALL checkOPETree('"+tableName+"','"+colName+"','"+insertPath+"',@irr)";
			Statement stmt=conn.createStatement();
			stmt.executeQuery(check);
			//��ȡ�Ƿ�������ת����
//			String qir="SELECT @irr";
//			ResultSet rs=stmt.executeQuery(qir);
//			rs.next();
//			int ird=rs.getInt("@irr");
//			if(ird==1) isRebalanced=true;
		}
//		return isRebalanced;
		return false;
	}
}
