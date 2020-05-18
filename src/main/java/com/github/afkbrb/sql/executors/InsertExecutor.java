package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.ast.statements.InsertStatement;
import com.github.afkbrb.sql.model.*;

import java.util.Arrays;
import java.util.List;

public class InsertExecutor extends Executor {

    public static void doInsert(InsertStatement insertStatement) throws SQLExecuteException {
        String tableName = insertStatement.getTableName();
        Table table = requireTableExists(tableName);

        Schema schema = table.getSchema();
        List<String> columnNameList = insertStatement.getColumnList();
        for (String columnName : columnNameList) {
            if (schema.getColumnIndex(columnName) == -1) {
                error("column %s doesn't exist", columnName);
            }
        }

        if (columnNameList.size() == 0) { // 省略了列，则表示全部列，此时就没必要检查列名是否合法了
            schema.getColumnList().forEach(column -> columnNameList.add(column.getColumnName()));
        }

        List<Expression> valueList = insertStatement.getValueList();
        if (columnNameList.size() != valueList.size()) {
            error("column count is %d, while value count is %d", columnNameList.size(), valueList.size());
        }

        Cell[] cells = new Cell[schema.getColumnCount()];

        for (int i = 0; i < columnNameList.size(); i++) {
            int cellIndex = schema.getColumnIndex(columnNameList.get(i));
            DataType expectedType = schema.getColumnType(cellIndex);
            TypedValue typedValue = evaluate(valueList.get(i)); // 我们要求插入的数据不包含列名
            cells[cellIndex] = new Cell(ensureDataType(expectedType, typedValue));
        }

        for (int i = 0; i < cells.length; i++) {
            if (cells[i] == null) cells[i] = new Cell(TypedValue.NULL);
        }

        Row row = new Row(Arrays.asList(cells));
        table.addRow(row);
    }
}
