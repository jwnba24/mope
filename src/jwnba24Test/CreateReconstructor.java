package jwnba24Test;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.util.deparser.CreateTableDeParser;
import ssdb.core.NameHide;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class CreateReconstructor {
    private CCJSqlParserManager parserManager = new CCJSqlParserManager();

    private StringBuilder buffer = new StringBuilder();

    public String createTableReconstruct(String sql) throws Exception {
        CreateTable createTable = (CreateTable) parserManager.parse(new
                StringReader(sql));
        CreateTableDeParser createTableDeparser = new CreateTableDeParser(buffer);
        List<ColumnDefinition> listColumn = createTable.getColumnDefinitions();
        String plainColumnName = "";
        String secretColumnName = "";
        ColDataType numericType = new ColDataType();
        numericType.setDataType("double");
        ColDataType stringType = new ColDataType();
        stringType.setDataType("text");
        List<ColumnDefinition> newListColumn = new ArrayList<ColumnDefinition>();
        for (int i = 0; i < listColumn.size();
             i++) {
            plainColumnName = listColumn.get(i).getColumnName();
            secretColumnName = NameHide.getSecretName(plainColumnName);
            if (listColumn.get(i).getColDataType().getDataType().toLowerCase().equals("int")
                    ) {
                ColumnDefinition element0 = new ColumnDefinition();
                element0.setColumnName(NameHide.getDETName(secretColumnName));
                element0.setColDataType(stringType);
                newListColumn.add(element0);
                ColumnDefinition element1 = new ColumnDefinition();
                element1.setColumnName(NameHide.getOPEName(secretColumnName));
                element1.setColDataType(numericType);
                newListColumn.add(element1);
            } else {
                if (listColumn.get(i).getColDataType().getDataType().toLowerCase().
                        equals("char")) {
                    ColumnDefinition element = new ColumnDefinition();
                    element.setColumnName(NameHide.getDETName(secretColumnName));
                    element.setColDataType(stringType);
                    newListColumn.add(element);
                }
            }
        }
        createTable.setColumnDefinitions(newListColumn);
        createTableDeparser.deParse(createTable);
        return buffer.toString() + ";";
    }
}