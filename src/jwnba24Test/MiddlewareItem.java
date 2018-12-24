package jwnba24Test;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/3/9.
 */
public class MiddlewareItem {
    public Map<String, Object> getItemList(String sql) throws Exception{
        CCJSqlParserManager psm = new CCJSqlParserManager();

        Map<String,Object> map=new HashMap<>();
        if (sql.startsWith("INSERT")) {
            Insert insert = (Insert) psm.parse(new StringReader(sql));
            List<Column> columnList=insert.getColumns();

            ExpressionList expressionList = (ExpressionList) insert.getItemsList();

            List<Expression> expressions=expressionList.getExpressions();

            for (int i=0;i<expressions.size();i++) {
                map.put(columnList.get(i).getColumnName(),expressions.get(i));
            }

        }

        return map;
    }
}
