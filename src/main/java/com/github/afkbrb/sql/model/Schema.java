package com.github.afkbrb.sql.model;

import com.github.afkbrb.sql.SQLExecuteException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Schema {

    // 多个列可能拥有相同的名字，所以此处使用了 list
    private final Map<String, List<Column>> nameToColumns = new HashMap<>();
    private final List<Column> columns;

    public static final Schema EMPTY_SCHEMA = new Schema(Collections.emptyList());

    public Schema(@NotNull List<Column> columns) {
        this.columns = Objects.requireNonNull(columns);
        for (Column column : columns) {
            nameToColumns.putIfAbsent(column.getColumnName().toLowerCase(), new ArrayList<>());
            nameToColumns.get(column.getColumnName().toLowerCase()).add(column);
        }
    }

    @Nullable
    public Column getColumn(@NotNull String tableName, @NotNull String columnName) throws SQLExecuteException {
        Objects.requireNonNull(tableName);
        Objects.requireNonNull(columnName);
        List<Column> columnList = nameToColumns.get(columnName.toLowerCase());
        if (columnList != null) {
            Column result = null;
            for (Column column : columnList) {
                if (column.getTableName().equalsIgnoreCase(tableName)) {
                    if (result == null) {
                        result = column;
                    } else {
                        throw new SQLExecuteException("Column '%s.%s' in field list is ambiguous", tableName, column.getColumnName());
                    }
                }
            }
            return result;
        }
        return null;
    }

    @Nullable
    public Column getColumn(@NotNull String columnName) throws SQLExecuteException {
        Objects.requireNonNull(columnName);
        List<Column> columnList = nameToColumns.get(columnName);
        if (columnList == null) return null;
        if (columnList.size() > 1)
            throw new SQLExecuteException(String.format("Column '%s' in field list is ambiguous", columnName));
        return columnList.get(0);
    }

    @NotNull
    public List<Column> getColumns() {
        return columns;
    }

    public int size() {
        return nameToColumns.size();
    }

}
