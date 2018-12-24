package jwnba24Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 转换sql语句
 * Created by Administrator on 2018/3/8.
 */

public class FormatSQL {

    /**
     * 加密查询语句
     *
     * @param sqls sql语句列表
     * @return
     */
    public static List<String> encSQL(List<String> sqls) throws JSQLParserException {
        List<String> resultList = new ArrayList<>();
        CCJSqlParserManager psm = new CCJSqlParserManager();

        List<Double> numList = getNumList(sqls);
        Map<Double, String> pathMap = getPathInfo(numList);

        for (String sql : sqls) {
            if (sql.startsWith("INSERT")) {
                Insert insert = (Insert) psm.parse(new StringReader(sql));
                List<Column> columnList = insert.getColumns();//获取插入的表名称列表
                ExpressionList expressionList = (ExpressionList) insert.getItemsList();

                List<Expression> expressions = expressionList.getExpressions();//获取插入列的列表
                insert.setColumns(rewriteColumns(columnList));
                insert.setItemsList(rewriteExpressionList(expressions, pathMap));
                resultList.add(insert.toString() + ";");
                System.out.println(insert.toString() + ";");
            }
        }
        return resultList;
    }

    /**
     * 获取节点二叉排序树路径
     *
     * @param numList
     */
    private static Map<Double, String> getPathInfo(List<Double> numList) {
        Map<Double, String> pathMap = new HashMap<>();

        double[] array = new double[numList.size()];
        for (int i = 0; i < numList.size(); i++) {
            array[i] = numList.get(i);
        }

        BinarySearchTree bst = new BinarySearchTree();
        for (double key : array) {
            bst.insert(key);
        }
        for (int i = 0; i < bst.getRoot().size(); i++) {
            List<TreeNode> list = bst.inOrderTraverseList(i);
            for (TreeNode node : list) {
                pathMap.put(node.getKey(), node.getIndex());
            }
        }

        return pathMap;

    }

    /**
     * 获取sql语句中值为数字的数据
     *
     * @param sqls
     * @return
     * @throws JSQLParserException
     */
    private static List<Double> getNumList(List<String> sqls) throws JSQLParserException {
        List<Double> resultList = new ArrayList<>();
        CCJSqlParserManager psm = new CCJSqlParserManager();
        for (String sql : sqls) {
            if (sql.startsWith("INSERT")) {
                Insert insert = (Insert) psm.parse(new StringReader(sql));
                ExpressionList expressionList = (ExpressionList) insert.getItemsList();
                List<Expression> expressions = expressionList.getExpressions();//获取插入列的列表

                for (Expression e : expressions) {
                    if (e instanceof LongValue) {
                        resultList.add(Double.parseDouble(((LongValue) e).getStringValue()));

                    } else if (e instanceof DoubleValue) {
                        resultList.add(Double.parseDouble(((LongValue) e).getStringValue()));

                    }

                }

            }
        }
        return resultList;
    }

    /**
     * 重写插入的列
     * @param columnList
     * @return
     */
    private static List<Column> rewriteColumns(List<Column> columnList) {
        List<Column> resultList = new ArrayList<Column>();
        for (Column c : columnList) {
            Column column = new Column("enc_" + c.getColumnName());
            resultList.add(column);
        }
        Column path_column = new Column("path");
        Column height_column = new Column("height");
        //添加两列path,height 代表数字型数据构造成平衡二叉树的路径和节点所在树的高度，测试用
        resultList.add(path_column);
        resultList.add(height_column);

        return resultList;
    }

    /**
     * 重写插入的值，如果值中存在数字，那么构造二叉树
     * @param expressions
     * @param pathMap
     * @return
     */
    private static ExpressionList rewriteExpressionList(List<Expression> expressions, Map<Double, String> pathMap) {
        ExpressionList resultList = new ExpressionList();
        //新的expressionlist来代替原来的
        List<Expression> newExpressionList = new ArrayList<Expression>();
        double temp = 0;

        for (Expression e : expressions) {
            String rightToStr = "";
            if (e instanceof LongValue) {
                temp = Double.parseDouble(((LongValue) e).getStringValue());
                rightToStr = "enc_" + ((LongValue) e).getStringValue();
            } else if (e instanceof DoubleValue) {
                temp = Double.parseDouble(((LongValue) e).getStringValue());
                rightToStr = "enc_" + String.valueOf(((DoubleValue) e).getValue());
            } else if (e instanceof StringValue) {
                rightToStr = "enc_" + ((StringValue) e).getValue();
            }
            newExpressionList.add(new StringValue("'" + rightToStr + "'"));
        }
        if (temp != 0) {
            newExpressionList.add(new StringValue("'" + pathMap.get(temp) + "'"));
            newExpressionList.add(new StringValue("'" + pathMap.get(temp).length() + "'"));
        }
        resultList.setExpressions(newExpressionList);


        return resultList;
    }

    public static void main(String[] args) throws Exception {
    }
}
