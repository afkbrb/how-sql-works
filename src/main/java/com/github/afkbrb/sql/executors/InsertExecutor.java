package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.ast.statements.InsertStatement;
import com.github.afkbrb.sql.model.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class InsertExecutor extends Executor {

    public static void doInsert(InsertStatement insertStatement) throws SQLExecuteException, IOException {
        String tableName = insertStatement.getTableName();
        Table table = requireTableExists(tableName);

        Schema schema = table.getSchema();
        List<String> columnNameList = insertStatement.getColumnList();
        if (columnNameList.size() == 0) { // 省略了列，则表示全部列，此时就没必要检查列名是否合法了
            schema.getColumns().forEach(column -> columnNameList.add(column.getColumnName()));
        }

        List<Expression> valueList = insertStatement.getValueList();
        if (columnNameList.size() != valueList.size()) {
            throw new SQLExecuteException("column count is %d, while value count is %d", columnNameList.size(), valueList.size());
        }

        Cell[] cells = new Cell[schema.size()];

        for (int i = 0; i < columnNameList.size(); i++) {
            Column column = schema.getColumn(columnNameList.get(i));
            if (column == null) throw new SQLExecuteException("Column %s doesn't exist", columnNameList.get(i));;
            DataType expectedType = column.getDataType();
            TypedValue typedValue = evaluate(valueList.get(i)); // 我们要求插入的数据不包含列名
            cells[column.getColumnIndex()] = new Cell(ensureDataType(expectedType, typedValue));
        }

        for (int i = 0; i < cells.length; i++) {
            if (cells[i] == null) cells[i] = new Cell(TypedValue.NULL);
        }

        Row row = new Row(Arrays.asList(cells));
        table.addRow(row);

        updateTable(table);
    }
}
