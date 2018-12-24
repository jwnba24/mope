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
 * mope ������
 * @author zenggo
 *
 */
public class MOPE {
	//mope index��λ�� ��32����֧�ֲ���2^31Լ20�ڸ���ͬ����ֵ
	public static final int MOPE_LENGTH=32;
	//***********************************************���߷���start**********************************************
	/**
	 * ��bytes����ת��Ϊ2���Ƶ��ַ���,�ҳ���ΪMOPE_LENGTH
	 * @param bytes
	 * @return
	 */
	public static String binary(byte[] bytes){
		String bString=""; 
		String ts=new BigInteger(1, bytes).toString(2);// �����1��������
		for(int i=MOPE_LENGTH-ts.length();i>0;i--){
			//����ǰ�ߵġ�0��
			bString+="0";
		}
		return bString+ts;
	}
	/**
	 * �ú������ڻ�ȡ�²���ֵ��OPE TREE�����Ʊ��� ��mOPE index
	 * @param path ����mOPE TREE�Ķ�����·��
	 * @return index �²���ڵ�ı���
	 */
	public static String buildIndex(String path){
		//����*100...
		String index=path+"1";
		while(index.length()<MOPE_LENGTH){
			index+="0";
		}
		return index;
	}
	/**
	 * ��һ��·��*����Ϊ*000...000
	 * @param path
	 * @return
	 */
	public static String calPrefix(String path){
		//����*000...
		while(path.length()<MOPE_LENGTH){
			path+="0";
		}
		return path;
	}
	/**
	 * �����·������������Сֵ�����ֵ�ڵ�
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
	 * �����ݿ��ȡĳmope����ĳֵ��Ӧ�ڵ��ope tree�߶�
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
	//***********************************************���߷���end**********************************************
	
	/**
	 * ���ݸ����ı����������������ģ��ҵ�������mope Tree�еĽڵ�λ�á��������ֵ�������Ѵ��ڵģ�Ҳ�����Ǵ����������ҵģ�����SELECT RANGE QUERY��
	 * @param mopeORcmope �Ƿ���Ҫ_height
	 * @param plaintext ������/��ѯ������ֵ
	 * @param cipher ��Ӧ������ ���ڲ�ѯ���ݿ����Ƿ��Ѿ����� 
	 * @param colName ��������
	 * @param tableName �ñ����
	 * @param datatype ���е�������������
	 * @return OPE TREE�иýڵ�ı��� ������ �Լ��Ƿ��Ѿ�����
	 * @throws Exception 
	 */
	public static mOPENodeInfo findMOPETreeNode(boolean mopeORcmope,String plaintext,String ciphertext,String colName,String tableName,String datatype,Connection conn) throws Exception{
		//Connection conn=Connector.openConnection();
		Statement stmt=conn.createStatement();
		//�ж����ݿ�ñ��и����Ƿ����и�ֵ
		//������������
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
			//���Ѵ��ڣ�ֱ�ӷ�����mope index��height,����·����¼Ϊ���·���ַ�
			int existedHeight=mopeORcmope?rs_index.getInt("height"):1;
			String existedIndex = binary(rs_index.getBytes("index"));
			return new mOPENodeInfo(ciphertext,existedHeight,existedIndex,"e");
		}else{
			//��¼��ѯ��·��
			String path="";
			//��¼��ǰ��ѯ�Ľڵ�index
			String nowNodeIndex="";
			String search="SELECT "+cipherColName_DET+" AS 'cipher' FROM "+tableName+" WHERE "+secretColName+"_index = b'";
			//��ȡ����DET��Կ
			Key detKey = KeyManager.generateDETKey(Connector.getMasterKey(), colName, "det");
			//������Ϊ32λ����·�����ܳ���31λ������OPE TREE��32�㶼�����˵�������������ʱ���ǣ���ֵ������У�2^31-1����ȡֵ�����ȡ32λ����
			while(path.length()<MOPE_LENGTH){
				nowNodeIndex=buildIndex(path);
				ResultSet rs_cipher=stmt.executeQuery(search+nowNodeIndex+"'");
				if(rs_cipher.next()){
					//����
					String nowNodeCiphertext=rs_cipher.getString("cipher");
					String nowNodePlaintext=DETAlgorithm.decrypt(nowNodeCiphertext, detKey);
					//�ַ������ݱȽϴ�С
					if(datatype.equals("varchar")){
						if(plaintext.compareTo(nowNodePlaintext)<0){
							path+="0";
						}else{
							path+="1";
						}
					//��ֵ�����ݱȽϴ�С
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
					//���ýڵ�Ϊ�գ����ҵ��˲���λ�� path
					break;
				}	
			}
			return new mOPENodeInfo(ciphertext,1,nowNodeIndex,path);
		}
	}
	/**
	 * ��checkAllBalance���� ��mysql��������ִ��procedure ���¼��ĳ��ĳ�в���path��ĸ߶ȡ�ƽ�����
	 * @param tableName Ҫ���м���table
	 * @param colName Ҫ���м�����
	 * @param insertPath ������ֵʱ��·��
	 * @throws Exception 
	 */
	public static boolean checkPath(String tableName,String colName,String insertPath,Connection conn) throws Exception{
		//boolean isRebalanced=false;
		//���������Ǹ��ڵ㣬������
		if(insertPath.length()==0){
			return false;
		}else{
			//�Ӹ�һ����path��ʼ����
			String path=insertPath.substring(0, insertPath.length()-1);
			String check="CALL checkPathAvl('"+tableName+"','"+NameHide.getSecretName(colName)+"','"+path+"',@ir)";
			//System.out.println(check);
			Statement stmt=conn.createStatement();
			stmt.executeQuery(check);
			//��ȡ�Ƿ�������ת����
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
	//************************************************����Ϊû�ᵽmysql�ϵĴ��룬��ɾ*********************************************************
	//***************************************************************************************************************************************
	/**
	 * ��checkAllBalance���� ��SERVER�����ظ��¼��ĳ��ĳ�в���path��ĸ߶ȡ�ƽ�����
	 * @param tableName Ҫ���м���table
	 * @param colName Ҫ���м�����
	 * @param insertPath ������ֵʱ��·��
	 * @throws Exception 
	 */
	public static boolean checkPath_1(String tableName,String colName,String insertPath,Connection conn) throws Exception{
		//��¼�Ƿ�������ƽ��
		boolean isReBalanced=false;
		//Connection conn=Connector.openConnection();
		//��������Ǹ��ڵ㣬������
		if(insertPath.length()==0){
			return false;
		}
		//�Ӹ�һ����path��ʼ����
		String path=insertPath.substring(0, insertPath.length()-1);
		//������������
		String secretColName=NameHide.getSecretName(colName);
		String CipherIndexColName=secretColName+"_index";
		String CipherHeightColName=CipherIndexColName+"_height";
		int length=path.length();
		//�趨��ѯ�ڵ�߶ȵ�sql���
		String select="SELECT "+CipherIndexColName+"_height AS 'height' FROM "+tableName+" WHERE "+CipherIndexColName+" = b?";
		PreparedStatement ps=conn.prepareStatement(select);
		//��ʼ����
		while(length>-1){
			String nowPath=path.substring(0,length);
			String nowNodeIndex=buildIndex(nowPath);
			//��ѯ��ǰ�ڵ�Ϊ���������߶�
			ps.setString(1, nowNodeIndex);
			int h_root=getHeightOfIndex(ps.executeQuery());
			//��ѯ�������߶�
			ps.setString(1, buildIndex(nowPath+"0"));
			int h_left=getHeightOfIndex(ps.executeQuery());
			//��ѯ�������߶�
			ps.setString(1, buildIndex(nowPath+"1"));
			int h_right=getHeightOfIndex(ps.executeQuery());
			//ͨ����ǰ�ڵ�����������߼������¸߶�
			int h_root_new=max(h_left,h_right)+1;
			if(h_root==h_root_new){
				//����ǰ�ڵ�߶Ȳ��䣬��ǰ�ڵ����丸�ڵ㲻���ܷ����߶ȱ仯Ҳ�Ͳ����ܷ�����ת����������
				break;
			}else{
				//��ʧ��
				if(Math.abs(h_left-h_right)==2){
					isReBalanced=true;
					//�ж���ת����
					if(h_left>h_right){
						//��ȡ��ǰ�ڵ����ӵ��������߶�
						ps.setString(1, buildIndex(nowPath+"00"));
						int h_left_left=getHeightOfIndex(ps.executeQuery());
						//��ȡ��ǰ�ڵ����ӵ��������߶�
						ps.setString(1, buildIndex(nowPath+"01"));
						int h_left_right=getHeightOfIndex(ps.executeQuery());
						if(h_left_left>h_left_right){
							//���ӵ�������������������ִ��LL��ת
							LLRotate(nowPath,CipherIndexColName,tableName,conn,h_right);
						}else{
							//���ӵ�������������������ִ��LR��ת
							LRRotate(nowPath,CipherIndexColName,tableName,conn,h_right);
						}
					}else{
						//��ȡ��ǰ�ڵ��Һ��ӵ��������߶�
						ps.setString(1, buildIndex(nowPath+"10"));
						int h_right_left=getHeightOfIndex(ps.executeQuery());
						//��ȡ��ǰ�ڵ��Һ��ӵ��������߶�
						ps.setString(1, buildIndex(nowPath+"11"));
						int h_right_right=getHeightOfIndex(ps.executeQuery());
						if(h_right_right>h_right_left){
							//�Һ��ӵ�������������������ִ��RR��ת
							RRRotate(nowPath,CipherIndexColName,tableName,conn,h_left);
						}else{
							//�Һ��ӵ�������������������ִ��RL��ת
							RLRotate(nowPath,CipherIndexColName,tableName,conn,h_left);
						}
					}
					//��ת��nowNodeIndex��Ӧ�Ľڵ�Ϊ���������߶���ԭ����ͬ���丸�ڵ㲻���ܷ����߶ȱ仯�Լ���ת����������
					break;
				}else{
					//��ʧ�⣬���µ�ǰ�ڵ�߶ȣ���������
					Statement stmt=conn.createStatement();
					String updateHeight="UPDATE "+tableName+" SET "+CipherHeightColName+" = ";
					stmt.executeUpdate(updateHeight+h_root_new+" WHERE "+CipherIndexColName+" = b'"+nowNodeIndex+"'");
				}
			}
		length--;
		}
		return isReBalanced;
	}
	//******************************************************��ת������ʼ******************************************************
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
		 * UDF��ת����������node_index  varchar(32),root_path varchar(32),type_rotate INT,type_range INT
		 * UDF��ת�����������ţ�
		 * type_rotate:1-LL,2-LR,3-RR,4-RL
		 * type_range:1,2,3,4
		 */
		public static void LLRotate(String rootPath,String cipherIndexColName,String tableName,Connection conn,int h_r) throws SQLException{
			Statement stmt=conn.createStatement();
			//����prefix
			String prefix=calPrefix(rootPath);
			String update="UPDATE "+tableName+" SET "+cipherIndexColName+" = b'";
			String upRotate="UPDATE "+tableName+" SET "+cipherIndexColName+" = rotate("+cipherIndexColName+","+rootPath.length()+",b'"+prefix+"',";
			//��תǰ��ת�����ĸ��ڵ�k1��index
			String k1_old=buildIndex(rootPath);
			//��ת��k1��index
			String k1_new=buildIndex(rootPath+"1");
			//ѡתǰk1������k2��index
			String k2_old=buildIndex(rootPath+"0");
			//��ת��k2��index
			String k2_new=k1_old;
			
			//��תǰk2��������D1 index��Χ ��indexǰ׺
			String prefix_D1=rootPath+"00";
			//��תǰk2��������D2 index��Χ
			String prefix_D2=rootPath+"01";
			//��תǰk1��������D3 index��Χ
			String prefix_D3=rootPath+"1";
			//��ת��k1�߶�
			int h_k1_new=h_r+1;
			
			stmt.clearBatch();
			//pt1.��ת������D3��index
			stmt.addBatch(upRotate+"1,3) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D3)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D3)+"'");
			//pt2.����k1��index
			stmt.addBatch(update+k1_new+"' WHERE "+cipherIndexColName+" = b'"+k1_old+"'");
			//pt3.����k2��index
			stmt.addBatch(update+k2_new+"' WHERE "+cipherIndexColName+" = b'"+k2_old+"'");
			//pt4.��ת������D2��index
			stmt.addBatch(upRotate+"1,2) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D2)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D2)+"'");
			//pt5.��ת������D1��index
			stmt.addBatch(upRotate+"1,1) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D1)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D1)+"'");
			//pt6.����k1,k2�ĸ߶� (k2�߶Ȳ���)
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k1_new+" WHERE "+cipherIndexColName+" = b'"+k1_new+"'");
			stmt.executeBatch();
		}
		public static void LRRotate(String rootPath,String cipherIndexColName,String tableName,Connection conn,int h_r) throws SQLException{
			Statement stmt=conn.createStatement();
			//����prefix
			String prefix=calPrefix(rootPath);
			String update="UPDATE "+tableName+" SET "+cipherIndexColName+" = b'";
			String upRotate="UPDATE "+tableName+" SET "+cipherIndexColName+" = rotate("+cipherIndexColName+","+rootPath.length()+",b'"+prefix+"',";
			//��תǰ��ת�����ĸ��ڵ�k1��index
			String k1_old=buildIndex(rootPath);
			//��ת��k1��index
			String k1_new=buildIndex(rootPath+"1");
			//ѡתǰk1������k2��index,k2��ת��index����
			String k2=buildIndex(rootPath+"0");
			//ѡתǰk2���Һ���k3��index
			String k3_old=buildIndex(rootPath+"01");
			//��ת��k3��index
			String k3_new=k1_old;
			//��תǰk2��������D1 index��Χ ��indexǰ׺
			String prefix_D1=rootPath+"00";
			//��תǰk3��������D2 index��Χ
			String prefix_D2=rootPath+"010";
			//��תǰk3��������D3 index��Χ
			String prefix_D3=rootPath+"011";
			//��תǰk1��������D4 index��Χ
			String prefix_D4=rootPath+"1";
			//��ת��k1,k2,k3�ĸ߶�  
			int h_k1_new=h_r+1;
			int h_k2_new=h_k1_new;
			int h_k3_new=h_k1_new+1;
			
			//pt1.��ת������D4��index
			stmt.addBatch(upRotate+"2,4) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D4)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D4)+"'");
			//pt2.��ת������k1��index
			stmt.addBatch(update+k1_new+"' WHERE "+cipherIndexColName+" = b'"+k1_old+"'");
			//pt3.��ת������k3��index
			stmt.addBatch(update+k3_new+"' WHERE "+cipherIndexColName+" = b'"+k3_old+"'");
			//pt4.��ת������D3��index
			stmt.addBatch(upRotate+"2,3) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D3)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D3)+"'");
			//pt5.��ת������D2��index
			stmt.addBatch(upRotate+"2,2) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D2)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D2)+"'");
			//pt6.����k1.k1.k3�߶�
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k1_new+" WHERE "+cipherIndexColName+" = b'"+k1_new+"'");
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k2_new+" WHERE "+cipherIndexColName+" = b'"+k2+"'");
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k3_new+" WHERE "+cipherIndexColName+" = b'"+k3_new+"'");
			stmt.executeBatch();
		}
		public static void RRRotate(String rootPath,String cipherIndexColName,String tableName,Connection conn,int h_l) throws SQLException{
			Statement stmt=conn.createStatement();
			//����prefix
			String prefix=calPrefix(rootPath);
			String update="UPDATE "+tableName+" SET "+cipherIndexColName+" = b'";
			String upRotate="UPDATE "+tableName+" SET "+cipherIndexColName+" = rotate("+cipherIndexColName+","+rootPath.length()+",b'"+prefix+"',";
			//��תǰ��ת�����ĸ��ڵ�k1��index
			String k1_old=buildIndex(rootPath);
			//��ת��k1��index
			String k1_new=buildIndex(rootPath+"0");
			//ѡתǰk1���Һ���k2��index
			String k2_old=buildIndex(rootPath+"1");
			//��ת��k2��index
			String k2_new=k1_old;
			//��תǰk2��������D1 index��Χ ��indexǰ׺
			String prefix_D1=rootPath+"11";
			//��תǰk2��������D2 index��Χ
			String prefix_D2=rootPath+"10";
			//��תǰk1��������D3 index��Χ
			String prefix_D3=rootPath+"0";
			//��ת��k1,k2�ĸ߶� (k2�߶Ȳ���)
			int h_k1_new=h_l+1;
			
			//pt1.��ת������D3��index
			stmt.addBatch(upRotate+"3,3) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D3)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D3)+"'");
			//pt2.����k1��index
			stmt.addBatch(update+k1_new+"' WHERE "+cipherIndexColName+" = b'"+k1_old+"'");
			//pt3.����k2��index
			stmt.addBatch(update+k2_new+"' WHERE "+cipherIndexColName+" = b'"+k2_old+"'");
			//pt4.��ת������D2��index 
			stmt.addBatch(upRotate+"3,2) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D2)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D2)+"'");
			//pt5.��ת������D1��index
			stmt.addBatch(upRotate+"3,1) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D1)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D1)+"'");
			//pt6.����k1.k2�ĸ߶�
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k1_new+" WHERE "+cipherIndexColName+" = b'"+k1_new+"'");
			stmt.executeBatch();
		}
		public static void RLRotate(String rootPath,String cipherIndexColName,String tableName,Connection conn,int h_l) throws SQLException{
			Statement stmt=conn.createStatement();
			//����prefix
			String prefix=calPrefix(rootPath);
			String update="UPDATE "+tableName+" SET "+cipherIndexColName+" = b'";
			String upRotate="UPDATE "+tableName+" SET "+cipherIndexColName+" = rotate("+cipherIndexColName+","+rootPath.length()+",b'"+prefix+"',";
			//��תǰ��ת�����ĸ��ڵ�k1��index
			String k1_old=buildIndex(rootPath);
			//��ת��k1��index
			String k1_new=buildIndex(rootPath+"0");
			//ѡתǰk1���Һ���k2��index,k2��ת��index����
			String k2=buildIndex(rootPath+"1");
			//ѡתǰk2������k3��index
			String k3_old=buildIndex(rootPath+"10");
			//��ת��k3��index
			String k3_new=k1_old;
			//��תǰk2��������D1 index��Χ ��indexǰ׺
			String prefix_D1=rootPath+"11";
			//��תǰk3��������D2 index��Χ
			String prefix_D2=rootPath+"101";
			//��תǰk3��������D3 index��Χ
			String prefix_D3=rootPath+"100";
			//��תǰk1��������D4 index��Χ
			String prefix_D4=rootPath+"0";
			//��ת��k1.k2.k3�߶�
			int h_k1_new=h_l+1;
			int h_k2_new=h_k1_new;
			int h_k3_new=h_k1_new+1;

			//pt1.��ת������D4��index
			stmt.addBatch(upRotate+"4,4) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D4)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D4)+"'");
			//pt2.��ת������k1��index
			stmt.addBatch(update+k1_new+"' WHERE "+cipherIndexColName+" = b'"+k1_old+"'");
			//pt3.��ת������k3��index
			stmt.addBatch(update+k3_new+"' WHERE "+cipherIndexColName+" = b'"+k3_old+"'");
			//pt4.��ת������D3��index 
			stmt.addBatch(upRotate+"4,3) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D3)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D3)+"'");
			//pt5.��ת������D2��index 
			stmt.addBatch(upRotate+"4,2) WHERE "+cipherIndexColName+" >= b'"+calMinOfPath(prefix_D2)+"' AND "+cipherIndexColName+" <= b'"+calMaxOfPath(prefix_D2)+"'");
			//pt6.����k1,k2,k3�ĸ߶�  
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k1_new+" WHERE "+cipherIndexColName+" = b'"+k1_new+"'");
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k2_new+" WHERE "+cipherIndexColName+" = b'"+k2+"'");
			stmt.addBatch("UPDATE "+tableName+" SET "+cipherIndexColName+"_height = "+h_k3_new+" WHERE "+cipherIndexColName+" = b'"+k3_new+"'");
			stmt.executeBatch();
		}
		//******************************************************��ת��������******************************************************

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
