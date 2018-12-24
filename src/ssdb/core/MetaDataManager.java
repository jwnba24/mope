package ssdb.core;


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

/**
 * ����ϣ�����������ʵ��Ԫ���ݹ�������ã������ݿ��е�Ԫ���ݱ��л�ȡ����Ϣ������ڱ���
 * @author WANGHAIWEI
 *
 */
public class MetaDataManager {

	private String tableName;
	private List<String> allColumnName;
	private Map<String,String> dataTypeMeta;
	private Map<String,String> opeKeyMeta;
	private Map<String,String> homKeyMeta;
	private Map<String,String> mOPEMeta;
	
	public MetaDataManager(String tableName){
		this.tableName=tableName;
		dataTypeMeta = new HashMap<String,String>();
		opeKeyMeta = new HashMap<String,String>();
		homKeyMeta = new HashMap<String,String>();
		allColumnName = new ArrayList<String>();
		mOPEMeta=new HashMap<String,String>();
		fetchMetaData(tableName);
	}

	/**
	 * ����������ڻ�ȡ���Ԫ���ݣ�����ָ�����е��������������columnNameList�У�������Ҫע�⣬��Щ������������ʽ��<br>
	 * @param con ���ݿ�����
	 * @param tableName ��ȡ�ĸ����Ԫ����
	 * @return
	 * @throws SQLException 
	 */
	
	/*
	public  List<String> getAllDETColumnName() throws SQLException{
		List<String> allEncColumnName = new ArrayList<String>();
		for(String name : allColumnName){
			try {
				allEncColumnName.add(NameHide.getDETName(NameHide.getSecretName(name)));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("MetaDataManager�޷���ȡ������Ϣ");
				e.printStackTrace();
			}
		}
		return allEncColumnName;
	}
	*/
	public List<String> getAllPlainColumnName(){
		return allColumnName;
	}
	
	/**
	 * ������������ڴ��������޸ı�Ľṹʱ������Ľṹ��Ϣ�洢��metadata���У����а��������֣������������������������͡�
	 * @param tableName ���Ǵ������Ǹ��������
	 * @param listColumn ����ʱ���е���Ϣ��������������������
	 */
	
//	public static void storeMetaData(String tableName,List<ColumnDefinition> listColumn){	
//		try {
//			Connection conn = ConnectionMySQL.openConnection();
//			
//			String insertMetaData = "insert into metadata(tablename,columnname,datatype,opekey,homkey) values(?,?,?,?,?)";			
//			PreparedStatement pstmt = conn.prepareStatement(insertMetaData);
//
//			for(int i = 0;i < listColumn.size();i++){
//				
//				pstmt.setString(1, tableName);
//				pstmt.setString(2, listColumn.get(i).getColumnName());
//				String dataType = listColumn.get(i).getColDataType().getDataType();
//				pstmt.setString(3, dataType);
//				//����Ҫ����ope�㷨����Կ�������������ֵ����ݣ�a,b,sens��������ַ��ͺ���ֵ�Ͷ���Ҫ
//				//Ŀǰ����Ĭ����ֵ������int���ͣ��������ǵ�sensΪ1
//				double[] opeKey = KeyManager.generateOpeKey(1.0);
//				pstmt.setString(4, opeKey[0] + "," +opeKey[1] + "," + opeKey[2]);
//				/*����hom����Կ��������Ҫ�������ĵ��������ͽ����жϣ��������ֵ�ͣ���Ҫ����һ��homkey
//				      ������ַ��ͣ���homkey����ΪNULL;
//				   homKeyʵ������һ��double�͵Ķ�ά���飬����������������һ���ַ���
//				      Ŀǰ����Ĭ����ֵ��Ϊint�ͣ��Ժ����������һЩ��
//				*/
//				if(dataType.equals("int")||dataType.equals("double")||dataType.equals("float")){
//					//��ά����Ĵ�СΪdouble[5][3]
//					double homKey[][] = KeyManager.generateHomKey();
//					StringBuilder keyBuffer = new StringBuilder();
//					for(int index_row = 0; index_row < 5 ;index_row++){
//						for(int index_col = 0;index_col < 3;index_col++){
//							keyBuffer.append(homKey[index_row][index_col]);
//							//����������һ��,�����һ��������Ϊ�зָ��
//							if(index_col != 2){
//								keyBuffer.append(",");
//							}
//						}
//						//����������һ��,�����һ���ֺ���Ϊ�зָ��
//						if(index_row != 4){
//							keyBuffer.append(";");
//						}
//					}
//					//��ת������Կ�ַ����洢�����ݿ���
//					pstmt.setString(5,keyBuffer.toString());
//				}else{
//					//����12������"varchar"����,���java.sql.Types
//					pstmt.setNull(5,12);
//				}
//				
//
//				//���е�Ԥռλ���ľ���ֵ���Ѿ����ú��ˣ�����Ϳ���ִ������Ԥ���������
//				//ע��������Ԥ����,����д��pstmt.executeUpdate(insertMetaData);����ᱨ��
//				pstmt.executeUpdate();
//			}
//			conn.close();
//			pstmt.close();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
	
	
	/**
	 * ��������������Ǹ��ݱ���tableName��metadata���л�ȡ����Ӧ�������е�Ԫ������Ϣ�������һ��map������
	 * �������������ִ������getXXX����֮ǰִ��
	 * @param tableName Ҫ��ȡ��Ϣ�ı���
	 * @return ����һ������<����,��������>��map����
	 */
	private void fetchMetaData(String tableName){
		try {		
			Connection conn = Connector.openConnection();
			Statement stmt = conn.createStatement();
			//Map<String,String> map = new HashMap<String,String>();
			ResultSet rs = stmt.executeQuery("select * from metadata where tablename = '" + tableName + "';");
			//���Ա���������Ԫ������Ϣ��
			while(rs.next()){
				String colName=rs.getString("columnname");
				dataTypeMeta.put(colName, rs.getString("datatype"));
				//opeKeyMeta.put(colName, rs.getString("opekey"));
				//homKeyMeta.put(colName, rs.getString("homkey"));	
				mOPEMeta.put(colName,rs.getString("mope"));
				allColumnName.add(rs.getString("columnname"));
			}
			conn.close();
			stmt.close();
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("��ȡԪ������Ϣʧ��");
		}
		
	}
	
	/**
	 * @return ����metadata��Ӧ�ı���
	 */
	public String getTableName(){
		return tableName;
	}
	
	/**
	 * @param columnName����
	 * @return ���ظ����Ƿ���mope����
	 */
	public String getMOPE(String columnName){
		if(!mOPEMeta.isEmpty()){
			return mOPEMeta.get(columnName);
		}else{
			System.out.println("���Ȼ�ȡԪ����");
			return null;
		}
	}
	/**
	 * �������������������ȡ�������ͣ�������ִ��fetchMetaData(String tableName)
	 * @param columnName ����
	 * @return �����ж�Ӧ�����ĵ���������
	 */
	public String getDataType(String columnName){		
		//�����Ϊ��
		if(!dataTypeMeta.isEmpty()){
			return dataTypeMeta.get(columnName);
		}else{
			System.out.println("���Ȼ�ȡԪ����");
			return null;
		}
	}	
	/**
	 * ����������ڻ�ȡָ���е�OPE��Կ��������ִ��fetchMetaData(String tableName)
	 * @param columnName ����
	 * @return ����OPE��Կ
	 */
	public double[] getOpeKey(String columnName){
		if(!opeKeyMeta.isEmpty()){
			String[] opeKeyStr = opeKeyMeta.get(columnName).split(",");
			double[] opeKey = new double[3];
			opeKey[0] = Double.valueOf(opeKeyStr[0]);
			opeKey[1] = Double.valueOf(opeKeyStr[1]);
			opeKey[2] = Double.valueOf(opeKeyStr[2]);
			return opeKey;
		}else{
			return null;
		}
	}

	/**
	 * ����������ڻ�ȡָ�����HOM��Կ��������ִ��fetchMetaData(String tableName)
	 * ��ͨ��fetchMetaData(String tableName)��������Ԫ���ݱ��л�ȡ��HOM��Կ�ַ�����Ȼ������ͨ���������ת����double��
	 */
	public double[][] getHomKey(String columnName){
		if(!homKeyMeta.isEmpty()){
			double[][] homKey = new double[5][3];		
			//hom��Կ��double[5][3]�͵Ķ�ά���飬��ת�����ַ���ʱ����";"�����зָ��","�����зָ�
			if(homKeyMeta.get(columnName) != null){
				String[] homKeyStr_row = homKeyMeta.get(columnName).split(";");
				for(int index_row = 0;index_row < homKeyStr_row.length ; index_row++){
					String[] homKeyStr_col = homKeyStr_row[index_row].split(",");
					for(int index_col = 0;index_col < homKeyStr_col.length;index_col++){
						homKey[index_row][index_col] = Double.valueOf(homKeyStr_col[index_col]);
					}
				}		
				return homKey;
			}else{
				//�����ǰ�в�����ֵ�ͣ���ôHOMԪ�����ǿգ��������ʾ
				System.out.println("��ǰ�е�HOM��ԿΪ�գ�");
				return null;
				}
		}else{
			//���homKeyMeta�ǿյģ�˵����û�л�ȡԪ���ݣ���Ҫ��ʾ�û���ִ��fetchMetaData(String tableName)
			System.out.println("���Ȼ�ȡԪ����");
			return null;
		}
	}
}
