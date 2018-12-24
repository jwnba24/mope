package ssdb.core.mope;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ssdb.core.NameHide;

/**
 * cmope 工具类
 * 可自定义调整策略
 * @author zenggo
 *
 */
public class CmOPE {
	//参数P
	public static double Percent=0.5;
	
	public static boolean checkOPETree(String tableName,String colName,String insertPath,Connection conn) throws Exception{
		//boolean isRebalanced=false;
		//如果插入的是根节点，不回溯
		if(insertPath.length()==0){
			return false;
		}else{
			String check="CALL checkOPETree('"+tableName+"','"+colName+"','"+insertPath+"',@irr)";
			Statement stmt=conn.createStatement();
			stmt.executeQuery(check);
			//获取是否发生了旋转操作
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
