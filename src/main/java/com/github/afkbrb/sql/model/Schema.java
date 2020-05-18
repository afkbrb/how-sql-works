package com.github.afkbrb.sql.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Schema {

    private final Map<String, List<Column>> nameToColumnList = new HashMap<>();
    private final List<Column> columnList;

    public Schema(@NotNull List<Column> columnList) {
        this.columnList = Objects.requireNonNull(columnList);
        for (Column column : columnList) {
            nameToColumnList.putIfAbsent(column.getColumnName().toLowerCase(), new ArrayList<>());
            nameToColumnList.get(column.getColumnName().toLowerCase()).add(column);
        }
    }

    public int getColumnCount() {
        return nameToColumnList.size();
    }

    @Nullable
    public Column getColumn(String columnName) {
        if (columnName.contains(".")) {
            // 如果 columnName 可能是 tableName.columnName 的形式
            String[] split = columnName.split("\\.");
            if (split.length != 2) throw new IllegalArgumentException("invalid columnName " + columnName);
            String tableName = split[0];
            String columnName1 = split[1];
            List<Column> columnList = nameToColumnList.get(columnName1.toLowerCase());
            if (columnList == null) return null;
            for (Column column : columnList) {
                if (column.getTableAlias().equalsIgnoreCase(tableName)) return column;
            }
            return null;
        } else {
            List<Column> columnList = nameToColumnList.get(columnName);
            if (columnList == null) return null;
            if (columnList.size() > 1)
                throw new IllegalArgumentException(String.format("column '%s' in field list is ambiguous", columnName));
            return columnList.get(0);
        }
    }

    public int getColumnIndex(String columnName) {
        Column column = getColumn(columnName);
        if (column == null) return -1;
        return column.getColumnIndex();
    }

    @Nullable
    public DataType getColumnType(String columnName) {
        Column column = getColumn(columnName);
        if (column == null) return null;
        return column.getDataType();
    }

    public DataType getColumnType(int index) {
        if (index >= 0 && index < columnList.size()) {
            return columnList.get(index).getDataType();
        }
        return null;
    }

    public List<Column> getColumnList() {
        return columnList;
    }
}
