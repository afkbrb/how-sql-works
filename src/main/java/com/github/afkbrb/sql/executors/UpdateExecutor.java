package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.ast.expressions.IntExpression;
import com.github.afkbrb.sql.ast.statements.UpdateStatement;
import com.github.afkbrb.sql.model.*;
import com.github.afkbrb.sql.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public class UpdateExecutor extends Executor {

    public static void doUpdate(UpdateStatement updateStatement) throws SQLExecuteException {
        String tableName = updateStatement.getTableName();
        Table table = requireTableExists(tableName);

        Schema schema = table.getSchema();

        Expression condition = updateStatement.getWhereCondition() == null ? new IntExpression(1) : updateStatement.getWhereCondition();
        List<Row> rows = table.getRows();
        List<Row> filteredRows = new ArrayList<>();
        for (Row row : rows) {
            if (predicate(schema, row, condition)) filteredRows.add(row);
        }

        List<Pair<String, Expression>> updateList = updateStatement.getUpdateList();
        for (Row row : filteredRows) {
            List<Cell> cells = row.getCells();
            for (Pair<String, Expression> pair : updateList) {
                Column column = schema.getColumn(pair.getKey());
                if (column == null) throw new SQLExecuteException("cannot find column %s in table %s", pair.getKey(), tableName);
                int index = column.getColumnIndex();
                DataType expectedType = column.getDataType();
                TypedValue typedValue = evaluate(schema, row, pair.getValue());
                cells.get(index).setTypedValue(ensureDataType(expectedType, typedValue));
            }
        }
    }
}
