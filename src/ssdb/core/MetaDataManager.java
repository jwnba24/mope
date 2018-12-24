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
 * 我们希望在这个类中实现元数据管理的作用，从数据库中的元数据表中获取到信息，存放在本地
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
	 * 这个函数用于获取表的元数据，并将指定表中的所有列名存放在columnNameList中，不过需要注意，这些列名是密文形式的<br>
	 * @param con 数据库连接
	 * @param tableName 获取哪个表的元数据
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
				System.out.println("MetaDataManager无法获取列名信息");
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
	 * 这个函数用于在创建或者修改表的结构时，将表的结构信息存储在metadata表中，其中包括三部分：表名、列名、列中数据类型。
	 * @param tableName 我们创建的那个表的名字
	 * @param listColumn 创建时的列的信息，包括列名和数据类型
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
//				//下面要设置ope算法的密钥，包含三个部分的内容，a,b,sens这个对于字符型和数值型都需要
//				//目前我们默认数值类型是int类型，所以我们的sens为1
//				double[] opeKey = KeyManager.generateOpeKey(1.0);
//				pstmt.setString(4, opeKey[0] + "," +opeKey[1] + "," + opeKey[2]);
//				/*对于hom的密钥，我们需要根据明文的数据类型进行判断，如果是数值型，需要生成一个homkey
//				      如果是字符型，则homkey设置为NULL;
//				   homKey实际上是一个double型的二维数组，我们这里把它处理成一个字符串
//				      目前我们默认数值型为int型，以后可以在完善一些。
//				*/
//				if(dataType.equals("int")||dataType.equals("double")||dataType.equals("float")){
//					//二维数组的大小为double[5][3]
//					double homKey[][] = KeyManager.generateHomKey();
//					StringBuilder keyBuffer = new StringBuilder();
//					for(int index_row = 0; index_row < 5 ;index_row++){
//						for(int index_col = 0;index_col < 3;index_col++){
//							keyBuffer.append(homKey[index_row][index_col]);
//							//如果不是最后一列,则添加一个逗号作为列分割符
//							if(index_col != 2){
//								keyBuffer.append(",");
//							}
//						}
//						//如果不是最后一行,则添加一个分号作为行分割符
//						if(index_row != 4){
//							keyBuffer.append(";");
//						}
//					}
//					//将转换的密钥字符串存储在数据库中
//					pstmt.setString(5,keyBuffer.toString());
//				}else{
//					//数字12代表了"varchar"类型,详见java.sql.Types
//					pstmt.setNull(5,12);
//				}
//				
//
//				//所有的预占位符的具体值都已经设置好了，下面就可以执行这条预处理语句了
//				//注意这里是预编译,不能写成pstmt.executeUpdate(insertMetaData);否则会报错
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
	 * 这个函数的作用是根据表名tableName从metadata表中获取到相应的所有列的元数据信息，存放在一个map集合中
	 * 这个函数必须在执行其他getXXX函数之前执行
	 * @param tableName 要获取信息的表名
	 * @return 返回一个含有<列名,数据类型>的map集合
	 */
	private void fetchMetaData(String tableName){
		try {		
			Connection conn = Connector.openConnection();
			Statement stmt = conn.createStatement();
			//Map<String,String> map = new HashMap<String,String>();
			ResultSet rs = stmt.executeQuery("select * from metadata where tablename = '" + tableName + "';");
			//向成员变量中添加元数据信息。
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
			System.out.println("获取元数据信息失败");
		}
		
	}
	
	/**
	 * @return 返回metadata对应的表名
	 */
	public String getTableName(){
		return tableName;
	}
	
	/**
	 * @param columnName列名
	 * @return 返回该列是否有mope索引
	 */
	public String getMOPE(String columnName){
		if(!mOPEMeta.isEmpty()){
			return mOPEMeta.get(columnName);
		}else{
			System.out.println("请先获取元数据");
			return null;
		}
	}
	/**
	 * 这个函数根据列名，获取数据类型，必须先执行fetchMetaData(String tableName)
	 * @param columnName 列名
	 * @return 返回列对应的明文的数据类型
	 */
	public String getDataType(String columnName){		
		//如果不为空
		if(!dataTypeMeta.isEmpty()){
			return dataTypeMeta.get(columnName);
		}else{
			System.out.println("请先获取元数据");
			return null;
		}
	}	
	/**
	 * 这个函数用于获取指定列的OPE密钥，必须先执行fetchMetaData(String tableName)
	 * @param columnName 列名
	 * @return 返回OPE密钥
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
	 * 这个函数用于获取指定类的HOM密钥。必须先执行fetchMetaData(String tableName)
	 * 先通过fetchMetaData(String tableName)函数，从元数据表中获取到HOM密钥字符串，然后我们通过这个函数转换成double型
	 */
	public double[][] getHomKey(String columnName){
		if(!homKeyMeta.isEmpty()){
			double[][] homKey = new double[5][3];		
			//hom密钥是double[5][3]型的二维数组，在转换成字符串时，用";"进行行分割，用","进行列分割
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
				//如果当前列不是数值型，那么HOM元数据是空，则给出提示
				System.out.println("当前列的HOM密钥为空！");
				return null;
				}
		}else{
			//如果homKeyMeta是空的，说明还没有获取元数据，需要提示用户先执行fetchMetaData(String tableName)
			System.out.println("请先获取元数据");
			return null;
		}
	}
}
