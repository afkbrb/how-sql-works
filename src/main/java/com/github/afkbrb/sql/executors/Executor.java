package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.TableManager;
import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.model.*;
import com.github.afkbrb.sql.visitors.RowEvaluator;
import com.github.afkbrb.sql.visitors.RowsEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.github.afkbrb.sql.model.DataType.DOUBLE;
import static com.github.afkbrb.sql.utils.DataTypeUtils.*;

public abstract class Executor {

    protected static void saveTable(Table table) throws IOException {
        TableManager.getInstance().saveTable(table);
    }

    @NotNull
    protected static TypedValue evaluate(Expression expression) throws SQLExecuteException {
        // 使用固定的空值，防止每次重新创建一个空实例
        return evaluate(Schema.EMPTY_SCHEMA, Row.EMPTY_ROW, expression);
    }

    @NotNull
    protected static TypedValue evaluate(InheritedContext context, Expression expression) throws SQLExecuteException {
        // 使用固定的空值，防止每次重新创建一个空实例
        return evaluate(context, Schema.EMPTY_SCHEMA, Row.EMPTY_ROW, expression);
    }

    protected static boolean predicate(Schema schema, Row row, Expression condition) throws SQLExecuteException {
        TypedValue typedValue = evaluate(schema, row, condition);
        return typedValue.getValue() instanceof Number && ((Number) typedValue.getValue()).intValue() != 0;
    }

    @NotNull
    protected static TypedValue evaluate(Schema schema, Row row, Expression expression) throws SQLExecuteException {
        return evaluate(null, schema, row, expression);
    }

    @NotNull
    protected static TypedValue evaluate(Schema schema, List<Row> rows, Expression expression) throws SQLExecuteException {
        return evaluate(null, schema, rows, expression);
    }

    public static boolean predicate(InheritedContext context, Schema schema, Row row, Expression expression) throws SQLExecuteException {
        TypedValue typedValue = evaluate(context, schema, row, expression);
        return typedValue.getValue() instanceof Number && ((Number) typedValue.getValue()).intValue() != 0;
    }

    public static boolean predicate(InheritedContext context, Schema schema, List<Row> rows, Expression expression) throws SQLExecuteException {
        TypedValue typedValue = evaluate(context, schema, rows, expression);
        return typedValue.getValue() instanceof Number && ((Number) typedValue.getValue()).intValue() != 0;
    }

    public static TypedValue evaluate(@Nullable InheritedContext context, @NotNull Schema schema,
                                      @NotNull Row row, @NotNull Expression expression) throws SQLExecuteException {
        RowEvaluator evaluator = new RowEvaluator(context, schema, row);
        TypedValue typedValue = evaluator.evaluate(expression);
        if (isError(typedValue)) throw new SQLExecuteException(typedValue.getValue().toString());
        return typedValue;
    }

    public static TypedValue evaluate(@Nullable InheritedContext context, @NotNull Schema schema,
                                      @NotNull List<Row> rows, @NotNull Expression expression) throws SQLExecuteException {
        RowsEvaluator evaluator = new RowsEvaluator(context, schema, rows);
        TypedValue typedValue = evaluator.evaluate(expression);
        if (isError(typedValue)) throw new SQLExecuteException(typedValue.getValue().toString());
        return typedValue;
    }

    protected static void requireTableNotExists(String tableName) throws SQLExecuteException {
        if (TableManager.getInstance().getTable(tableName) != null) {
            throw new SQLExecuteException("table %s already exists", tableName);
        }
    }

    protected static Table requireTableExists(String tableName) throws SQLExecuteException {
        Table table = TableManager.getInstance().getTable(tableName);
        if (table == null) {
            throw new SQLExecuteException("table %s doesn't exist!", tableName);
        }
        return table;
    }

    /**
     * 确保 typedValue 的类型是 type 类型或者可以转换成 type 类型。
     */
    protected static TypedValue ensureDataType(@NotNull DataType type, @NotNull TypedValue typedValue) throws SQLExecuteException {
        Objects.requireNonNull(type);
        Objects.requireNonNull(typedValue);
        //  所有的 throw new SQLExecuteException 已经由 evaluate 相关函数处理了，此处不再处理
        if (isNull(typedValue)) return TypedValue.NULL;
        if (typedValue.getDataType() != type) {
            if (type == DOUBLE && isInt(typedValue)) {
                return new TypedValue(DataType.DOUBLE, ((Number) typedValue.getValue()).doubleValue());
            } else if (type == DataType.INT && isDouble(typedValue)) {
                return new TypedValue(DataType.INT, ((Number) typedValue.getValue()).intValue());
            } else {
                throw new SQLExecuteException("expected %s, but got %s", type, typedValue.getDataType());
            }
        } else {
            return typedValue;
        }
    }
}
