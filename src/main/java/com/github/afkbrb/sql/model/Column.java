package com.github.afkbrb.sql.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Column {

    private int columnIndex;
    private String columnName;
    private DataType dataType;
    private String tableAlias;

    public Column(int columnIndex, @NotNull String columnName, @NotNull DataType dataType, @NotNull String tableAlias) {
        this.columnIndex = columnIndex;
        this.columnName = Objects.requireNonNull(columnName);
        this.dataType = Objects.requireNonNull(dataType);
        this.tableAlias = Objects.requireNonNull(tableAlias);
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
    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(@NotNull String tableAlias) {
        this.tableAlias = Objects.requireNonNull(tableAlias);
    }

    @Override
    public String toString() {
        return "{" +
                "\"index\": " + columnIndex + ", " +
                "\"name\": " + "\"" + columnName + "\"" + ", " +
                "\"type\": " + "\"" + dataType + "\"" + ", " +
                "\"tableAlias\": " + "\"" + tableAlias + "\"" +
                "}";
    }
}
