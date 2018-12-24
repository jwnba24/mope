package ssdb.deparser;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ssdb.core.Connector;
import ssdb.core.DETAlgorithm;
import ssdb.core.KeyManager;

import ssdb.core.MetaDataManager;
import ssdb.core.mope.CmOPE;
import ssdb.core.mope.MOPE;
import ssdb.core.mope.mOPENodeInfo;
import ssdb.core.NameHide;
import ssdb.demo.Test;
import ssdb.demo.Times;
//import com.ssdb.core.DETAlgorithm;

import net.sf.jsqlparser.expression.BitsValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.insert.Insert;

/**
 * 本INSERT暂时只支持INT DOUBLE VARCHAR;
 * 并假定传入的INSERT语句正确，且形式皆为INSERT INTO table(col1,col2,...) VALUES (val1,val2,...) 表名后包含列名
 * @author zenggo
 * @extends haiwei
 */

public class MOPE_InsertDeparser {
	//记录insert的表中有mope index的列的密文列名前缀
	private List<String> mopeColumnNames;
	//记录该列对应的插入新值的ope tree路径
	private List<String> mopeColInsertPath;
	//记录该列采用的是mope还是cmope
	private List<String> mopeORcmope;
	//记录某列某值的mope index与tree height
	public List<String> getMopeColumnNames(){
		return mopeColumnNames;
	}
	public List<String> getMopeColInsertPath(){
		return mopeColInsertPath;
	}
	public List<String> getMopeORcmope(){
		return mopeORcmope;
	}
	private MOPE_InsertDeparser(){
		this.mopeColumnNames=new ArrayList<String>();
		this.mopeColInsertPath=new ArrayList<String>();
		this.mopeORcmope=new ArrayList<String>();
	}
	/**
	 * 根据原始insert语句与metadata重构insert
	 * @param insert
	 * @return 重构后的insert语句字符串
	 * @throws Exception 
	 */
	public String sqlReconstruct(Insert insert,Connection conn) throws Exception{
		MetaDataManager metaManager=new MetaDataManager(insert.getTable().getName());
		//记录原insert语句中的columns
		List<Column> list = insert.getColumns();
		//重写insert语句中的columns
		insert.setColumns(rewriteColumnList(list,metaManager));
		//重写insert语句中的expressionList
		ExpressionList expressionList = (ExpressionList) insert.getItemsList();
		insert.setItemsList(rewriteExpressionList(list,expressionList.getExpressions(),metaManager,conn));
		return insert.toString() + ";";
	}
	/**
	 * 根据原始insert语句中的插入列与metadata重构插入列
	 * @param plainColumnList 原始insert中插入列
	 * @param metaManager 被插入的表的元数据
	 * @return 根据元数据表重构后的插入列
	 * @throws Exception 
	 */
	private List<Column> rewriteColumnList(List<Column> plainColumnList,MetaDataManager metaManager) throws Exception{
		String plainColumnName =""; 
		String secretColumnName="";
		String cipherColumnName_DET = ""; 
		List<Column> resultList = new ArrayList<Column>(); 
		for(int i = 0; i < plainColumnList.size(); i++){
			plainColumnName = plainColumnList.get(i).getColumnName();
			String dataType = metaManager.getDataType(plainColumnName);
			String isMope=metaManager.getMOPE(plainColumnName);
			//计算密文列名
			secretColumnName=NameHide.getSecretName(plainColumnName);
			cipherColumnName_DET=NameHide.getDETName(secretColumnName);
			Column c_DET=new Column(cipherColumnName_DET);
			//插入明文列名 用于测试 正式版删除
			resultList.add(plainColumnList.get(i));
			resultList.add(c_DET);
			// 明文列名，密文DET列名，密文DET_index，密文DET_index_height
			if(isMope.equals("1")){
				//insert语句中插入该列的index、height列名
				Column c_index=new Column(secretColumnName+"_index");
				Column c_height=new Column(secretColumnName+"_index_height");
				resultList.add(c_index);
				resultList.add(c_height);
				//记录有mope index的明文列名
				mopeColumnNames.add(secretColumnName);
				mopeORcmope.add("1");
			}else if(isMope.equals("2")){
				Column c_index=new Column(secretColumnName+"_index");
				resultList.add(c_index);
				//记录有mope index的明文列名
				mopeColumnNames.add(secretColumnName);
				mopeORcmope.add("2");
			}
		}
		return resultList;
	}
	/**
	 * 重写（加密，计算mope index）插入的值
	 * @param columnList 原始insert中插入列
	 * @param expressionList 原始insert中插入的明文值
	 * @param metaManager 被插入的表的元数据
	 * @return 重写后的插入值，包括密文、mope index、MOPE TREE的height等等
	 * @throws Exception 
	 */
	private ExpressionList rewriteExpressionList(List<Column> columnList, List<Expression> expressionList,MetaDataManager metaManager,Connection conn) throws Exception{
		ExpressionList resultList = new ExpressionList();
		//新的expressionlist来代替原来的
		List<Expression> newExpressionList = new ArrayList<Expression>();
		for (int index_column = 0; index_column < columnList.size(); index_column++) {
			//获取该列名，数值类型，是否mope
			String colName=columnList.get(index_column).getColumnName();
			String isMope=metaManager.getMOPE(colName);
			String datatype=metaManager.getDataType(colName);
			//获取inerst该列对应插入的明文值，并转换为字符串，提供给det加密函数
			Expression rightExp=expressionList.get(index_column);
			String rightToStr="";
			if(rightExp instanceof LongValue){
				rightToStr = ((LongValue) rightExp).getStringValue();
			}else if(rightExp instanceof DoubleValue){
				rightToStr = String.valueOf(((DoubleValue) rightExp).getValue());
			}else if(rightExp instanceof StringValue){
				rightToStr = ((StringValue)rightExp).getValue();
			}
			//插入明文，测试用，正式删
			newExpressionList.add(new StringValue("'"+rightToStr+"'"));
			//DET加密
			Key detKey = KeyManager.generateDETKey(Connector.getMasterKey(), colName, "det");
			String detEnc = DETAlgorithm.encrypt(rightToStr, detKey);
			//对任何数值类型det加密后都存储为字符串，在数据库中det加密的列数值类型都为varchar
			newExpressionList.add(new StringValue("'"+detEnc+"'"));
			//如果该列有mope
			if(isMope.equals("1")){
				//计算该值新插入时的mope index 这里path与index用字符串表示 再转换为long类型插入 在OPE table中用bigint存储 (bit串有时会出现getLong出错？)
				mOPENodeInfo iah=MOPE.findMOPETreeNode(true,rightToStr,detEnc,colName,metaManager.getTableName(),datatype,conn);
				newExpressionList.add(new BitsValue(iah.getIndex()));
				//记录有插入路径
				mopeColInsertPath.add(iah.getPath());
				//新节点树高
				newExpressionList.add(new LongValue(iah.getHeight()));
			//cmOPE
			}else if(isMope.equals("2")){
				mOPENodeInfo iah=MOPE.findMOPETreeNode(false,rightToStr,detEnc,colName,metaManager.getTableName(),datatype,conn);
				newExpressionList.add(new BitsValue(iah.getIndex()));
				//记录有插入路径
				mopeColInsertPath.add(iah.getPath());
			}
		}
		resultList.setExpressions(newExpressionList);
		return resultList; 
	}
	/**
	 * 被Client主函数，开始重构
	 * @param insert 主函数传来的原始insert
	 * @param conn 
	 * @throws Exception 
	 */
	//public static boolean handler(Insert insert,Connection conn) throws Exception{
	public static Times handler(Insert insert,Connection conn) throws Exception{
		long start = System.currentTimeMillis();
		Statement stmt=conn.createStatement();
		MOPE_InsertDeparser ind=new MOPE_InsertDeparser();
		String outputSQL=ind.sqlReconstruct(insert,conn);
		//System.out.println(outputSQL);
		long end=System.currentTimeMillis();
		stmt.executeUpdate(outputSQL);
		//开始向SERVER发送UDF请求，检查节点平衡并更新
		long end_2=System.currentTimeMillis();
		
		//boolean isReBalanced=false;
		for(int i=0;i<ind.getMopeColumnNames().size();i++){
			//若该插入路径为"e"，则表示插入此值时table中已存在该值，则本列不需要回溯更新高度与检查平衡	
			if(ind.getMopeColInsertPath().get(i).equals("e")) continue;
			//boolean isReBalanced;
			if(ind.getMopeORcmope().get(i).equals("1")){
				//mope AVL树调整
				//isReBalanced=MOPE.checkPath(insert.getTable().getName(),ind.getMopeColumnNames().get(i),ind.getMopeColInsertPath().get(i),conn);
				MOPE.checkPath(insert.getTable().getName(),ind.getMopeColumnNames().get(i),ind.getMopeColInsertPath().get(i),conn);
			}else{
				//cmope 调整
				//isReBalanced=CmOPE.checkOPETree(insert.getTable().getName(),ind.getMopeColumnNames().get(i),ind.getMopeColInsertPath().get(i),conn);
				CmOPE.checkOPETree(insert.getTable().getName(),ind.getMopeColumnNames().get(i),ind.getMopeColInsertPath().get(i),conn);
			}
		}
		//return isReBalanced;
		return new Times(end-start,end_2-end,System.currentTimeMillis()-end_2);
	}
}