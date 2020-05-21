package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.ast.expressions.IntExpression;
import com.github.afkbrb.sql.ast.statements.DeleteStatement;
import com.github.afkbrb.sql.model.Row;
import com.github.afkbrb.sql.model.Table;

import java.util.Iterator;
import java.util.List;

public class DeleteExecutor extends Executor {

    public static void doDelete(DeleteStatement deleteStatement) throws SQLExecuteException {
        String tableName = deleteStatement.getTableName();
        Table table = requireTableExists(tableName);
        List<Row> rows = table.getRows();
        Expression condition = deleteStatement.getWhereCondition() == null ? new IntExpression(1) : deleteStatement.getWhereCondition();
        for (Iterator<Row> iterator = rows.iterator(); iterator.hasNext(); ) {
            if (predicate(table.getSchema(), iterator.next(), condition)) iterator.remove();
        }
    }
}
