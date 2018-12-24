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
	 * @param radix
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
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
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
			//A.addSL(0);
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
			//A.addSL(path.length()+1);
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
			String check="CALL checkPathAvl('"+tableName+"','"+colName+"','"+path+"',@ir)";
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
}
