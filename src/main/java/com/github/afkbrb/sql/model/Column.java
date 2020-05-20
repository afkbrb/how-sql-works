package com.github.afkbrb.sql.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Column {

    private final int columnIndex;
    private final String columnName;
    private final DataType dataType;
    private final String tableName;

    public Column(int columnIndex, @NotNull String columnName, @NotNull DataType dataType, @NotNull String tableName) {
        this.columnIndex = columnIndex;
        this.columnName = Objects.requireNonNull(columnName);
        this.dataType = Objects.requireNonNull(dataType);
        this.tableName = Objects.requireNonNull(tableName);
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    @NotNull
    public String getColumnName() {
        return columnName;
    }

    @NotNull
    public DataType getDataType() {
        return dataType;
    }

    @NotNull
    public String getTableName() {
        return tableName;
    }

    @Override
    public String toString() {
        return "{" +
                "\"index\": " + columnIndex + ", " +
                "\"name\": " + "\"" + columnName + "\"" + ", " +
                "\"type\": " + "\"" + dataType + "\"" + ", " +
                "\"tableAlias\": " + "\"" + tableName + "\"" +
                "}";
    }
}
