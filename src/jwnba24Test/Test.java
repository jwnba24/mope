package jwnba24Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;

import java.io.StringReader;
import java.util.*;

/**
 * Created by Administrator on 2018/3/8.
 */
public class Test {
    public static void main(String[] args) throws Exception {
        List<String> sqls = new ArrayList<String>();
        sqls.add("INSERT INTO students(name,score) VALUES ('zrk',88)");
        sqls.add("INSERT INTO students(name,score) VALUES ('alex',88)");
        sqls.add("INSERT INTO students(name,score) VALUES ('arsenal',14)");
        sqls.add("INSERT INTO students(name,score) VALUES ('Akun',24)");
        sqls.add("INSERT INTO students(name,score) VALUES ('monreal',6)");
        sqls.add("INSERT INTO students(name,score) VALUES ('eva',5)");
        sqls.add("INSERT INTO students(name,score) VALUES ('Akun',18)");
        sqls.add("INSERT INTO students(name,score) VALUES ('Akun',9)");
        sqls.add("INSERT INTO students(name,score) VALUES ('Akun',7)");
        sqls.add("INSERT INTO students(name,score) VALUES ('Akun',10)");
        sqls.add("INSERT INTO students(name,score) VALUES ('kyo',65)");
        sqls.add("INSERT INTO students(name,score) VALUES ('nerd',75)");

        List<String> list = FormatSQL.encSQL(sqls);

        Dao.insertBatch(list);

    }

}
