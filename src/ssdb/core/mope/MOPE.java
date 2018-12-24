package ssdb.core.mope;

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
import ssdb.demo.A;
import ssdb.deparser.MOPE_InsertDeparser;
import ssdb.core.NameHide;
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
	 * @param radix
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
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
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
			//A.addSL(0);
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
			//A.addSL(path.length()+1);
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
			String check="CALL checkPathAvl('"+tableName+"','"+colName+"','"+path+"',@ir)";
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
}
