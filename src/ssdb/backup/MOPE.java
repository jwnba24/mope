package ssdb.backup;

import java.math.BigInteger;
import java.security.Key;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import ssdb.core.Connector;
import ssdb.core.DETAlgorithm;
import ssdb.core.KeyManager;
import ssdb.deparser.MOPE_InsertDeparser;
import ssdb.core.NameHide;
import ssdb.core.mope.mOPENodeInfo;
/**
 * mope 工具类
 * @author zenggo
 *
 */
public class MOPE {
	//mope index的位数 如32，则支持插入2^31约20亿个不同的数值
	public static final int MOPE_LENGTH=32;
	//***********************************************工具方法start**********************************************
	/**
	 * 将bytes数组转化为2进制的字符串,且长度为MOPE_LENGTH
	 * @param bytes
	 * @return
	 */
	public static String binary(byte[] bytes){
		String bString=""; 
		String ts=new BigInteger(1, bytes).toString(2);// 这里的1代表正数
		for(int i=MOPE_LENGTH-ts.length();i>0;i--){
			//补齐前边的“0”
			bString+="0";
		}
		return bString+ts;
	}
	/**
	 * 该函数用于获取新插入值的OPE TREE二进制编码 即mOPE index
	 * @param path 插入mOPE TREE的二进制路径
	 * @return index 新插入节点的编码
	 */
	public static String buildIndex(String path){
		//补齐*100...
		String index=path+"1";
		while(index.length()<MOPE_LENGTH){
			index+="0";
		}
		return index;
	}
	/**
	 * 将一条路径*补齐为*000...000
	 * @param path
	 * @return
	 */
	public static String calPrefix(String path){
		//补齐*000...
		while(path.length()<MOPE_LENGTH){
			path+="0";
		}
		return path;
	}
	/**
	 * 计算该路径下子树的最小值与最大值节点
	 * @param path
	 * @return
	 */
	public static String calMinOfPath(String path){
		String index=new String(path);
		while(index.length()<MOPE_LENGTH-1){
			index+="0";
		}
		index+="1";
		return index;
	}
	public static String calMaxOfPath(String path){
		String index=new String(path);
		while(index.length()<MOPE_LENGTH){
			index+="1";
		}
		return index;
	}
	/**
	 * 向数据库获取某mope列中某值对应节点的ope tree高度
	 * @return
	 * @throws SQLException
	 */
	public static int getHeightOfIndex(ResultSet rs) throws SQLException{
		int h=rs.next()?rs.getInt("height"):0;
		return h;
	}
	public static int max(int a,int b){
		return a>b?a:b;
	}
	//***********************************************工具方法end**********************************************
	
	/**
	 * 根据给定的表、明文列名、明密文，找到密文在mope Tree中的节点位置。这个密文值可以是已存在的，也可以是待插入或待查找的（用于SELECT RANGE QUERY）
	 * @param mopeORcmope 是否需要_height
	 * @param plaintext 待插入/查询的明文值
	 * @param cipher 对应的密文 用于查询数据库中是否已经存在 
	 * @param colName 该列列名
	 * @param tableName 该表表名
	 * @param datatype 该列的明文数据类型
	 * @return OPE TREE中该节点的编码 与树高 以及是否已经存在
	 * @throws Exception 
	 */
	public static mOPENodeInfo findMOPETreeNode(boolean mopeORcmope,String plaintext,String ciphertext,String colName,String tableName,String datatype,Connection conn) throws Exception{
		//Connection conn=Connector.openConnection();
		Statement stmt=conn.createStatement();
		//判断数据库该表中该列是否已有该值
		//计算密文列名
		String secretColName=NameHide.getSecretName(colName);
		String cipherColName_DET=NameHide.getDETName(secretColName);
		String query="";
		if(mopeORcmope){
			query="SELECT "+secretColName+"_index AS 'index',"+secretColName+"_index_height AS 'height' FROM "+tableName+" WHERE "+cipherColName_DET+" = '"+ciphertext+"'";
		}else{
			query="SELECT "+secretColName+"_index AS 'index' FROM "+tableName+" WHERE "+cipherColName_DET+" = '"+ciphertext+"'";
		}
		ResultSet rs_index=stmt.executeQuery(query);
		if(rs_index.next()){
			//若已存在，直接返回其mope index与height,插入路径记录为填非路径字符
			int existedHeight=mopeORcmope?rs_index.getInt("height"):1;
			String existedIndex = binary(rs_index.getBytes("index"));
			return new mOPENodeInfo(ciphertext,existedHeight,existedIndex,"e");
		}else{
			//记录查询的路径
			String path="";
			//记录当前查询的节点index
			String nowNodeIndex="";
			String search="SELECT "+cipherColName_DET+" AS 'cipher' FROM "+tableName+" WHERE "+secretColName+"_index = b'";
			//获取该列DET密钥
			Key detKey = KeyManager.generateDETKey(Connector.getMasterKey(), colName, "det");
			//若编码为32位，则路径不能超过31位，考虑OPE TREE第32层都填满了的情况。不过设计时考虑，若值域最多有（2^31-1）个取值，则采取32位编码
			while(path.length()<MOPE_LENGTH){
				nowNodeIndex=buildIndex(path);
				ResultSet rs_cipher=stmt.executeQuery(search+nowNodeIndex+"'");
				if(rs_cipher.next()){
					//解密
					String nowNodeCiphertext=rs_cipher.getString("cipher");
					String nowNodePlaintext=DETAlgorithm.decrypt(nowNodeCiphertext, detKey);
					//字符型数据比较大小
					if(datatype.equals("varchar")){
						if(plaintext.compareTo(nowNodePlaintext)<0){
							path+="0";
						}else{
							path+="1";
						}
					//数值型数据比较大小
					}else{
						double plainNum=Double.valueOf(plaintext);
						double cipherNum=Double.valueOf(nowNodePlaintext);
						if(plainNum<cipherNum){
							path+="0";
						}else{
							path+="1";
						}
					}
				}else{
					//若该节点为空，则找到了插入位置 path
					break;
				}	
			}
			return new mOPENodeInfo(ciphertext,1,nowNodeIndex,path);
		}
	}
	/**
	 * 被checkAllBalance调用 向mysql发送请求执行procedure 更新检查某表某列插入path后的高度、平衡情况
	 * @param tableName 要进行检查的table
	 * @param colName 要进行检查的列
	 * @param insertPath 插入新值时的路径
	 * @throws Exception 
	 */
	public static boolean checkPath(String tableName,String colName,String insertPath,Connection conn) throws Exception{
		//boolean isRebalanced=false;
		//如果插入的是根节点，不回溯
		if(insertPath.length()==0){
			return false;
		}else{
			//从父一级的path开始回溯
			String path=insertPath.substring(0, insertPath.length()-1);
			String check="CALL checkPathAvl('"+tableName+"','"+NameHide.getSecretName(colName)+"','"+path+"',@ir)";
			//System.out.println(check);
			Statement stmt=conn.createStatement();
			stmt.executeQuery(check);
			//获取是否发生了旋转操作
//			String qir="SELECT @ir";
//			ResultSet rs=stmt.executeQuery(qir);
//			rs.next();
//			int ird=rs.getInt("@ir");
//			if(ird==1) isRebalanced=true;
		}
		//return isRebalanced;
		return false;
	}
	
	
	
	
	//***************************************************************************************************************************************
	//************************************************以下为没搬到mysql上的代码，可删*********************************************************
	//***************************************************************************************************************************************
	/**
	 * 被checkAllBalance调用 与SERVER交互地更新检查某表某列插入path后的高度、平衡情况
	 * @param tableName 要进行检查的table
	 * @param colName 要进行检查的列
	 * @param insertPath 插入新值时的路径
	 * @throws Exception 
	 */
	public static boolean checkPath_1(String tableName,String colName,String insertPath,Connection conn) throws Exception{
		//记录是否发生重新平衡
		boolean isReBalanced=false;
		//Connection conn=Connector.openConnection();
		//如果检查的是根节点，不回溯
		if(insertPath.length()==0){
			return false;
		}
		//从父一级的path开始回溯
		String path=insertPath.substring(0, insertPath.length()-1);
		//计算密文列名
		String secretColName=NameHide.getSecretName(colName);
		String CipherIndexColName=secretColName+"_index";
		String CipherHeightColName=CipherIndexColName+"_height";
		int length=path.length();
		//设定查询节点高度的sql语句
		String select="SELECT "+CipherIndexColName+"_height AS 'height' FROM "+tableName+" WHERE "+CipherIndexColName+" = b?";
		PreparedStatement ps=conn.prepareStatement(select);
		//开始回溯
		while(length>-1){
			String nowPath=path.substring(0,length);
			String nowNodeIndex=buildIndex(nowPath);
			//查询当前节点为根的子树高度
			ps.setString(1, nowNodeIndex);
			int h_root=getHeightOfIndex(ps.executeQuery());
			//查询左子树高度
			ps.setString(1, buildIndex(nowPath+"0"));
			int h_left=getHeightOfIndex(ps.executeQuery());
			//查询右子树高度
			ps.setString(1, buildIndex(nowPath+"1"));
			int h_right=getHeightOfIndex(ps.executeQuery());
			//通过当前节点的左右子树高计算其新高度
			int h_root_new=max(h_left,h_right)+1;
			if(h_root==h_root_new){
				//若当前节点高度不变，则当前节点与其父节点不可能发生高度变化也就不可能发生旋转，结束回溯
				break;
			}else{
				//若失衡
				if(Math.abs(h_left-h_right)==2){
					isReBalanced=true;
					//判断旋转类型
					if(h_left>h_right){
						//获取当前节点左孩子的左子树高度
						ps.setString(1, buildIndex(nowPath+"00"));
						int h_left_left=getHeightOfIndex(ps.executeQuery());
						//获取当前节点左孩子的右子树高度
						ps.setString(1, buildIndex(nowPath+"01"));
						int h_left_right=getHeightOfIndex(ps.executeQuery());
						if(h_left_left>h_left_right){
							//左孩子的左子树高于右子树，执行LL旋转
							LLRotate(nowPath,CipherIndexColName,tableName,conn,h_right);
						}else{
							//左孩子的右子树高于左子树，执行LR旋转
							LRRotate(nowPath,CipherIndexColName,tableName,conn,h_right);
						}
					}else{
						//获取当前节点右孩子的左子树高度
						ps.setString(1, buildIndex(nowPath+"10"));
						int h_right_left=getHeightOfIndex(ps.executeQuery());
						//获取当前节点右孩子的右子树高度
						ps.setString(1, buildIndex(nowPath+"11"));
						int h_right_right=getHeightOfIndex(ps.executeQuery());
						if(h_right_right>h_right_left){
							//右孩子的右子树高于左子树，执行RR旋转
							RRRotate(nowPath,CipherIndexColName,tableName,conn,h_left);
						}else{
							//右孩子的左子树高于右子树，执行RL旋转
							RLRotate(nowPath,CipherIndexColName,tableName,conn,h_left);
						}
					}
					//旋转后，nowNodeIndex对应的节点为根的子树高度与原来相同，其父节点不可能发生高度变化以及旋转，结束回溯
					break;
				}else{
					//不失衡，更新当前节点高度，继续回溯
					Statement stmt=conn.createStatement();
					String updateHeight="UPDATE "+tableName+" SET "+CipherHeightColName+" = ";
					stmt.executeUpdate(updateHeight+h_root_new+" WHERE "+CipherIndexColName+" = b'"+nowNodeIndex+"'");
				}
			}
		length--;
		}
		return isReBalanced;
	}
	//******************************************************旋转函数开始******************************************************
		/**
		 * 
		 * @param rootPath
		 * @param cipherIndexColName
		 * @param tableName
		 * @param stmt
		 * @param h_r
		 * @throws SQLException
		 */
		/*
		 * UDF旋转函数参数：node_index  varchar(32),root_path varchar(32),type_rotate INT,type_range INT
		 * UDF旋转函数参数代号：
		 * type_rotate:1-LL,2-LR,3-RR,4-RL
		 * type_range:1,2,3,4
		 */
		public static void LLRotate(String rootPath,String cipherIndexColName,String tableName,Connection conn,int h_r) throws SQLException{
			Statement stmt=conn.createStatement();
			//计算prefix
			String prefix=calPrefix(rootPath);
			String update="UPDATE "+tableName+" SET "+cipherIndexColName+" = b'";
			String upRotate="UPDATE "+tableName+" SET "+cipherIndexColName+" = rotate("+cipherIndexColName+","+rootPath.length()+",b'"+prefix+"',";
			//旋转前旋转子树的根节点k1的index
			String k1_old=buildIndex(rootPath);
			//旋转后k1的index
			String k1_new=buildIndex(rootPath+"1");
			//选转前k1的左孩子k2的index
			String k2_old=buildIndex(rootPath+"0");
			//旋转后k2的index
			String k2_new=k1_old;
			
			//旋转前k2的左子树D1 index范围 即index前缀
			String prefix_D1=rootPath+"00";
			//旋转前k2的右子树D2 index范围
			String prefix_D2=rootPath+"01";
			//旋转前k1的右子树D3 index范围
			String prefix_D3=rootPath+"1";
			//旋转后k1高度
			int h_k1_new=h_r+1;
			
			stmt.clearBatch();
			//pt1.旋转，更新D3的index
			stmt.addBatch(upRotate+"1,3) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D3)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D3)+"'");
			//pt2.更新k1的index
			stmt.addBatch(update+k1_new+"' WHERE "+cipherIndexColName+" = b'"+k1_old+"'");
			//pt3.更新k2的index
			stmt.addBatch(update+k2_new+"' WHERE "+cipherIndexColName+" = b'"+k2_old+"'");
			//pt4.旋转，更新D2的index
			stmt.addBatch(upRotate+"1,2) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D2)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D2)+"'");
			//pt5.旋转，更新D1的index
			stmt.addBatch(upRotate+"1,1) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D1)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D1)+"'");
			//pt6.更新k1,k2的高度 (k2高度不变)
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k1_new+" WHERE "+cipherIndexColName+" = b'"+k1_new+"'");
			stmt.executeBatch();
		}
		public static void LRRotate(String rootPath,String cipherIndexColName,String tableName,Connection conn,int h_r) throws SQLException{
			Statement stmt=conn.createStatement();
			//计算prefix
			String prefix=calPrefix(rootPath);
			String update="UPDATE "+tableName+" SET "+cipherIndexColName+" = b'";
			String upRotate="UPDATE "+tableName+" SET "+cipherIndexColName+" = rotate("+cipherIndexColName+","+rootPath.length()+",b'"+prefix+"',";
			//旋转前旋转子树的根节点k1的index
			String k1_old=buildIndex(rootPath);
			//旋转后k1的index
			String k1_new=buildIndex(rootPath+"1");
			//选转前k1的左孩子k2的index,k2旋转后index不变
			String k2=buildIndex(rootPath+"0");
			//选转前k2的右孩子k3的index
			String k3_old=buildIndex(rootPath+"01");
			//旋转后k3的index
			String k3_new=k1_old;
			//旋转前k2的左子树D1 index范围 即index前缀
			String prefix_D1=rootPath+"00";
			//旋转前k3的左子树D2 index范围
			String prefix_D2=rootPath+"010";
			//旋转前k3的右子树D3 index范围
			String prefix_D3=rootPath+"011";
			//旋转前k1的右子树D4 index范围
			String prefix_D4=rootPath+"1";
			//旋转后k1,k2,k3的高度  
			int h_k1_new=h_r+1;
			int h_k2_new=h_k1_new;
			int h_k3_new=h_k1_new+1;
			
			//pt1.旋转，更新D4的index
			stmt.addBatch(upRotate+"2,4) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D4)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D4)+"'");
			//pt2.旋转，更新k1的index
			stmt.addBatch(update+k1_new+"' WHERE "+cipherIndexColName+" = b'"+k1_old+"'");
			//pt3.旋转，更新k3的index
			stmt.addBatch(update+k3_new+"' WHERE "+cipherIndexColName+" = b'"+k3_old+"'");
			//pt4.旋转，更新D3的index
			stmt.addBatch(upRotate+"2,3) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D3)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D3)+"'");
			//pt5.旋转，更新D2的index
			stmt.addBatch(upRotate+"2,2) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D2)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D2)+"'");
			//pt6.更新k1.k1.k3高度
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k1_new+" WHERE "+cipherIndexColName+" = b'"+k1_new+"'");
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k2_new+" WHERE "+cipherIndexColName+" = b'"+k2+"'");
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k3_new+" WHERE "+cipherIndexColName+" = b'"+k3_new+"'");
			stmt.executeBatch();
		}
		public static void RRRotate(String rootPath,String cipherIndexColName,String tableName,Connection conn,int h_l) throws SQLException{
			Statement stmt=conn.createStatement();
			//计算prefix
			String prefix=calPrefix(rootPath);
			String update="UPDATE "+tableName+" SET "+cipherIndexColName+" = b'";
			String upRotate="UPDATE "+tableName+" SET "+cipherIndexColName+" = rotate("+cipherIndexColName+","+rootPath.length()+",b'"+prefix+"',";
			//旋转前旋转子树的根节点k1的index
			String k1_old=buildIndex(rootPath);
			//旋转后k1的index
			String k1_new=buildIndex(rootPath+"0");
			//选转前k1的右孩子k2的index
			String k2_old=buildIndex(rootPath+"1");
			//旋转后k2的index
			String k2_new=k1_old;
			//旋转前k2的右子树D1 index范围 即index前缀
			String prefix_D1=rootPath+"11";
			//旋转前k2的左子树D2 index范围
			String prefix_D2=rootPath+"10";
			//旋转前k1的左子树D3 index范围
			String prefix_D3=rootPath+"0";
			//旋转后k1,k2的高度 (k2高度不变)
			int h_k1_new=h_l+1;
			
			//pt1.旋转，更新D3的index
			stmt.addBatch(upRotate+"3,3) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D3)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D3)+"'");
			//pt2.更新k1的index
			stmt.addBatch(update+k1_new+"' WHERE "+cipherIndexColName+" = b'"+k1_old+"'");
			//pt3.更新k2的index
			stmt.addBatch(update+k2_new+"' WHERE "+cipherIndexColName+" = b'"+k2_old+"'");
			//pt4.旋转，更新D2的index 
			stmt.addBatch(upRotate+"3,2) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D2)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D2)+"'");
			//pt5.旋转，更新D1的index
			stmt.addBatch(upRotate+"3,1) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D1)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D1)+"'");
			//pt6.更新k1.k2的高度
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k1_new+" WHERE "+cipherIndexColName+" = b'"+k1_new+"'");
			stmt.executeBatch();
		}
		public static void RLRotate(String rootPath,String cipherIndexColName,String tableName,Connection conn,int h_l) throws SQLException{
			Statement stmt=conn.createStatement();
			//计算prefix
			String prefix=calPrefix(rootPath);
			String update="UPDATE "+tableName+" SET "+cipherIndexColName+" = b'";
			String upRotate="UPDATE "+tableName+" SET "+cipherIndexColName+" = rotate("+cipherIndexColName+","+rootPath.length()+",b'"+prefix+"',";
			//旋转前旋转子树的根节点k1的index
			String k1_old=buildIndex(rootPath);
			//旋转后k1的index
			String k1_new=buildIndex(rootPath+"0");
			//选转前k1的右孩子k2的index,k2旋转后index不变
			String k2=buildIndex(rootPath+"1");
			//选转前k2的左孩子k3的index
			String k3_old=buildIndex(rootPath+"10");
			//旋转后k3的index
			String k3_new=k1_old;
			//旋转前k2的右子树D1 index范围 即index前缀
			String prefix_D1=rootPath+"11";
			//旋转前k3的右子树D2 index范围
			String prefix_D2=rootPath+"101";
			//旋转前k3的左子树D3 index范围
			String prefix_D3=rootPath+"100";
			//旋转前k1的左子树D4 index范围
			String prefix_D4=rootPath+"0";
			//旋转后k1.k2.k3高度
			int h_k1_new=h_l+1;
			int h_k2_new=h_k1_new;
			int h_k3_new=h_k1_new+1;

			//pt1.旋转，更新D4的index
			stmt.addBatch(upRotate+"4,4) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D4)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D4)+"'");
			//pt2.旋转，更新k1的index
			stmt.addBatch(update+k1_new+"' WHERE "+cipherIndexColName+" = b'"+k1_old+"'");
			//pt3.旋转，更新k3的index
			stmt.addBatch(update+k3_new+"' WHERE "+cipherIndexColName+" = b'"+k3_old+"'");
			//pt4.旋转，更新D3的index 
			stmt.addBatch(upRotate+"4,3) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D3)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D3)+"'");
			//pt5.旋转，更新D2的index 
			stmt.addBatch(upRotate+"4,2) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D2)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D2)+"'");
			//pt6.更新k1,k2,k3的高度  
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k1_new+" WHERE "+cipherIndexColName+" = b'"+k1_new+"'");
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k2_new+" WHERE "+cipherIndexColName+" = b'"+k2+"'");
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k3_new+" WHERE "+cipherIndexColName+" = b'"+k3_new+"'");
			stmt.executeBatch();
		}
		//******************************************************旋转函数结束******************************************************

	public static boolean isPalindrome(int x) {

		int result=0;
		int temp=x;;
		while(temp!=0){
			if((long) Math.abs(result*10)>Integer.MAX_VALUE){
				result=0;
			}
			result=result*10+temp%10;
			temp=temp/10;
		}

		if(result==x){
			return true;
		}else{
			return false;
		}
	}

	public static void main(String[] args) {
		if(isPalindrome(1)){
			System.out.println(1);
		}
	}
}
