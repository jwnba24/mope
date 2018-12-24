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
 * ��INSERT��ʱֻ֧��INT DOUBLE VARCHAR;
 * ���ٶ������INSERT�����ȷ������ʽ��ΪINSERT INTO table(col1,col2,...) VALUES (val1,val2,...) �������������
 * @author zenggo
 * @extends haiwei
 */

public class MOPE_InsertDeparser {
	//��¼insert�ı�����mope index���е���������ǰ׺
	private List<String> mopeColumnNames;
	//��¼���ж�Ӧ�Ĳ�����ֵ��ope tree·��
	private List<String> mopeColInsertPath;
	//��¼���в��õ���mope����cmope
	private List<String> mopeORcmope;
	//��¼ĳ��ĳֵ��mope index��tree height
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
	 * ����ԭʼinsert�����metadata�ع�insert
	 * @param insert
	 * @return �ع����insert����ַ���
	 * @throws Exception 
	 */
	public String sqlReconstruct(Insert insert,Connection conn) throws Exception{
		MetaDataManager metaManager=new MetaDataManager(insert.getTable().getName());
		//��¼ԭinsert����е�columns
		List<Column> list = insert.getColumns();
		//��дinsert����е�columns
		insert.setColumns(rewriteColumnList(list,metaManager));
		//��дinsert����е�expressionList
		ExpressionList expressionList = (ExpressionList) insert.getItemsList();
		insert.setItemsList(rewriteExpressionList(list,expressionList.getExpressions(),metaManager,conn));
		return insert.toString() + ";";
	}
	/**
	 * ����ԭʼinsert����еĲ�������metadata�ع�������
	 * @param plainColumnList ԭʼinsert�в�����
	 * @param metaManager ������ı��Ԫ����
	 * @return ����Ԫ���ݱ��ع���Ĳ�����
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
			//������������
			secretColumnName=NameHide.getSecretName(plainColumnName);
			cipherColumnName_DET=NameHide.getDETName(secretColumnName);
			Column c_DET=new Column(cipherColumnName_DET);
			//������������ ���ڲ��� ��ʽ��ɾ��
			resultList.add(plainColumnList.get(i));
			resultList.add(c_DET);
			// ��������������DET����������DET_index������DET_index_height
			if(isMope.equals("1")){
				//insert����в�����е�index��height����
				Column c_index=new Column(secretColumnName+"_index");
				Column c_height=new Column(secretColumnName+"_index_height");
				resultList.add(c_index);
				resultList.add(c_height);
				//��¼��mope index����������
				mopeColumnNames.add(secretColumnName);
				mopeORcmope.add("1");
			}else if(isMope.equals("2")){
				Column c_index=new Column(secretColumnName+"_index");
				resultList.add(c_index);
				//��¼��mope index����������
				mopeColumnNames.add(secretColumnName);
				mopeORcmope.add("2");
			}
		}
		return resultList;
	}
	/**
	 * ��д�����ܣ�����mope index�������ֵ
	 * @param columnList ԭʼinsert�в�����
	 * @param expressionList ԭʼinsert�в��������ֵ
	 * @param metaManager ������ı��Ԫ����
	 * @return ��д��Ĳ���ֵ���������ġ�mope index��MOPE TREE��height�ȵ�
	 * @throws Exception 
	 */
	private ExpressionList rewriteExpressionList(List<Column> columnList, List<Expression> expressionList,MetaDataManager metaManager,Connection conn) throws Exception{
		ExpressionList resultList = new ExpressionList();
		//�µ�expressionlist������ԭ����
		List<Expression> newExpressionList = new ArrayList<Expression>();
		for (int index_column = 0; index_column < columnList.size(); index_column++) {
			//��ȡ����������ֵ���ͣ��Ƿ�mope
			String colName=columnList.get(index_column).getColumnName();
			String isMope=metaManager.getMOPE(colName);
			String datatype=metaManager.getDataType(colName);
			//��ȡinerst���ж�Ӧ���������ֵ����ת��Ϊ�ַ������ṩ��det���ܺ���
			Expression rightExp=expressionList.get(index_column);
			String rightToStr="";
			if(rightExp instanceof LongValue){
				rightToStr = ((LongValue) rightExp).getStringValue();
			}else if(rightExp instanceof DoubleValue){
				rightToStr = String.valueOf(((DoubleValue) rightExp).getValue());
			}else if(rightExp instanceof StringValue){
				rightToStr = ((StringValue)rightExp).getValue();
			}
			//�������ģ������ã���ʽɾ
			newExpressionList.add(new StringValue("'"+rightToStr+"'"));
			//DET����
			Key detKey = KeyManager.generateDETKey(Connector.getMasterKey(), colName, "det");
			String detEnc = DETAlgorithm.encrypt(rightToStr, detKey);
			//���κ���ֵ����det���ܺ󶼴洢Ϊ�ַ����������ݿ���det���ܵ�����ֵ���Ͷ�Ϊvarchar
			newExpressionList.add(new StringValue("'"+detEnc+"'"));
			//���������mope
			if(isMope.equals("1")){
				//�����ֵ�²���ʱ��mope index ����path��index���ַ�����ʾ ��ת��Ϊlong���Ͳ��� ��OPE table����bigint�洢 (bit����ʱ�����getLong����)
				mOPENodeInfo iah=MOPE.findMOPETreeNode(true,rightToStr,detEnc,colName,metaManager.getTableName(),datatype,conn);
				newExpressionList.add(new BitsValue(iah.getIndex()));
				//��¼�в���·��
				mopeColInsertPath.add(iah.getPath());
				//�½ڵ�����
				newExpressionList.add(new LongValue(iah.getHeight()));
			//cmOPE
			}else if(isMope.equals("2")){
				mOPENodeInfo iah=MOPE.findMOPETreeNode(false,rightToStr,detEnc,colName,metaManager.getTableName(),datatype,conn);
				newExpressionList.add(new BitsValue(iah.getIndex()));
				//��¼�в���·��
				mopeColInsertPath.add(iah.getPath());
			}
		}
		resultList.setExpressions(newExpressionList);
		return resultList; 
	}
	/**
	 * ��Client����������ʼ�ع�
	 * @param insert ������������ԭʼinsert
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
		//��ʼ��SERVER����UDF���󣬼��ڵ�ƽ�Ⲣ����
		long end_2=System.currentTimeMillis();
		
		//boolean isReBalanced=false;
		for(int i=0;i<ind.getMopeColumnNames().size();i++){
			//���ò���·��Ϊ"e"�����ʾ�����ֵʱtable���Ѵ��ڸ�ֵ�����в���Ҫ���ݸ��¸߶�����ƽ��	
			if(ind.getMopeColInsertPath().get(i).equals("e")) continue;
			//boolean isReBalanced;
			if(ind.getMopeORcmope().get(i).equals("1")){
				//mope AVL������
				//isReBalanced=MOPE.checkPath(insert.getTable().getName(),ind.getMopeColumnNames().get(i),ind.getMopeColInsertPath().get(i),conn);
				MOPE.checkPath(insert.getTable().getName(),ind.getMopeColumnNames().get(i),ind.getMopeColInsertPath().get(i),conn);
			}else{
				//cmope ����
				//isReBalanced=CmOPE.checkOPETree(insert.getTable().getName(),ind.getMopeColumnNames().get(i),ind.getMopeColInsertPath().get(i),conn);
				CmOPE.checkOPETree(insert.getTable().getName(),ind.getMopeColumnNames().get(i),ind.getMopeColInsertPath().get(i),conn);
			}
		}
		//return isReBalanced;
		return new Times(end-start,end_2-end,System.currentTimeMillis()-end_2);
	}
}