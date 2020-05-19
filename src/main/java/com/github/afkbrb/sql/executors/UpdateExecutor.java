package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.ast.expressions.IntExpression;
import com.github.afkbrb.sql.ast.statements.UpdateStatement;
import com.github.afkbrb.sql.model.*;
import com.github.afkbrb.sql.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateExecutor extends Executor {

    public static void doUpdate(UpdateStatement updateStatement) throws SQLExecuteException {
        String tableName = updateStatement.getTableName();
        Table table = requireTableExists(tableName);

        Schema schema = table.getSchema();
        List<Pair<String, Expression>> updateList = updateStatement.getUpdateList();
        for (Pair<String, Expression> pair : updateList) {
            if (schema.getColumnIndex(pair.getKey()) == -1) {
                error("cannot find column %s in table %s", pair.getKey(), tableName);
            }
        }

        Expression condition = updateStatement.getWhereCondition() == null ? new IntExpression(1) : updateStatement.getWhereCondition();
        List<Row> rows = table.getRows();
        List<Row> filteredRows = new ArrayList<>();
        for (Row row : rows) {
            if (predict(row, schema, condition)) filteredRows.add(row);
        }
        for (Row row : filteredRows) {
            List<Cell> cells = row.getCells();
            for (Pair<String, Expression> pair : updateList) {
                int index = schema.getColumnIndex(pair.getKey());
                DataType expectedType = schema.getColumnType(index);
                TypedValue typedValue = evaluate(row, schema, pair.getValue());
                cells.get(index).setTypedValue(ensureDataType(expectedType, typedValue));
            }
        }
    }
}
