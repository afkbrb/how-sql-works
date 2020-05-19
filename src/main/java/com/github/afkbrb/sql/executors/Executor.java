package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.TableManager;
import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.model.*;
import com.github.afkbrb.sql.visitors.RowEvaluator;
import com.github.afkbrb.sql.visitors.RowsEvaluator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static com.github.afkbrb.sql.model.DataType.DOUBLE;
import static com.github.afkbrb.sql.utils.DataTypeUtils.*;

public abstract class Executor {

    /**
     * 判断给定的 row 是否满足给定的条件。
     */
    protected static boolean predict(Row row, Schema schema, Expression condition) throws SQLExecuteException {
        TypedValue typedValue = evaluate(row, schema, condition);
        return typedValue.getValue() instanceof Number && ((Number) typedValue.getValue()).intValue() != 0;
    }

    protected static boolean predict(List<Row> rows, Schema schema, Expression condition) throws SQLExecuteException {
        TypedValue typedValue = evaluate(rows, schema, condition);
        return typedValue.getValue() instanceof Number && ((Number) typedValue.getValue()).intValue() != 0;
    }

    @NotNull
    protected static TypedValue evaluate(Row row, Schema schema, Expression expression) throws SQLExecuteException {
        RowEvaluator evaluator = new RowEvaluator(row, schema);
        TypedValue typedValue = evaluator.evaluate(expression);
        if (isError(typedValue)) error(typedValue.getValue().toString());
        return typedValue;
    }

    @NotNull
    protected static TypedValue evaluate(List<Row> rows, Schema schema, Expression expression) throws SQLExecuteException {
        RowsEvaluator evaluator = new RowsEvaluator(rows, schema);
        TypedValue typedValue = evaluator.evaluate(expression);
        if (isError(typedValue)) error(typedValue.getValue().toString());
        return typedValue;
    }

    @NotNull
    protected static TypedValue evaluate(Expression expression) throws SQLExecuteException {
        return evaluate((Row) null, null, expression);
    }

    protected static void error(String format, Object... args) throws SQLExecuteException {
        throw new SQLExecuteException(String.format(format, args));
    }

    protected static void requireTableNotExists(String tableName) throws SQLExecuteException {
        if (TableManager.getInstance().getTable(tableName) != null) {
            error("table %s already exists", tableName);
        }
    }

    protected static Table requireTableExists(String tableName) throws SQLExecuteException {
        Table table = TableManager.getInstance().getTable(tableName);
        if (table == null) {
            error("table %s doesn't exist!", tableName);
        }
        return table;
    }

    /**
     * 确保 typedValue 的类型是 type 类型或者可以转换成 type 类型。
     */
    protected static TypedValue ensureDataType(@NotNull DataType type, @NotNull TypedValue typedValue) throws SQLExecuteException {
        Objects.requireNonNull(type);
        Objects.requireNonNull(typedValue);
        //  所有的 error 已经由 evaluate 相关函数处理了，此处不再处理
        if (isNull(typedValue)) return TypedValue.NULL;
        if (typedValue.getDataType() != type) {
            if (type == DOUBLE && isInt(typedValue)) {
                return new TypedValue(DataType.DOUBLE, ((Number) typedValue.getValue()).doubleValue());
            } else if (type == DataType.INT && isDouble(typedValue)) {
                return new TypedValue(DataType.INT, ((Number) typedValue.getValue()).intValue());
            } else {
                error("expected %s, but got %s", type, typedValue.getDataType());
            }
        } else {
            return typedValue;
        }

        throw new IllegalStateException("got a bug, shouldn't reach here");
    }
}
