package com.github.afkbrb.sql.model;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 相当于符号表，保存了外层查询 columnName -> TypedValue 信息。
 */
public class InheritedContext {

    // 当作 Stack 来使用
    private final List<Pair<Schema, Row>> context = new LinkedList<>();

    /**
     * 根据表名和列名获取值。
     * 找不到的话返回 null，找到多个的话报错。
     */
    @SuppressWarnings("ConstantConditions")
    @Nullable
    public TypedValue getTypedValue(String tableName, String columnName) throws SQLExecuteException {
        for (Pair<Schema, Row> pair : context) {
            Schema schema = pair.getKey();
            Row row = pair.getValue();
            Column column = schema.getColumn(tableName, columnName);
            if (column != null) {
                return row.getCell(column.getColumnIndex()).getTypedValue();
            }
        }
        return null;
    }

    /**
     * 根据列名获取值。
     * 找不到的话返回 null，找到多个的话报错。
     */
    @SuppressWarnings("ConstantConditions")
    @Nullable
    public TypedValue getTypedValue(String columnName) throws SQLExecuteException {
        for (Pair<Schema, Row> pair : context) {
            Schema schema = pair.getKey();
            Row row = pair.getValue();
            Column column = schema.getColumn(columnName);
            if (column != null) {
                return row.getCell(column.getColumnIndex()).getTypedValue();
            }
        }
        return null;
    }

    /**
     * 添加新的 context 信息。
     */
    public void push(@NotNull Schema schema, @NotNull Row row) {
        Objects.requireNonNull(schema);
        Objects.requireNonNull(row);
        if (schema.size() != row.size()) throw new IllegalArgumentException("schema and row must have the same size");
        context.add(0, new Pair<>(schema, row)); // 添加到头部
    }

    /**
     * 删除最近添加的 context 信息。
     */
    public void pop() {
        if (context.size() == 0) throw new IllegalStateException("context is empty");
        context.remove(0); // 移除头部
    }
}
