package com.github.afkbrb.sql.model;

public class Column {

    private int columnIndex;
    private String columnName;
    private DataType dataType;
    private String tableAlias;

    public Column(int columnIndex, String columnName, DataType dataType, String tableAlias) {
        this.columnIndex = columnIndex;
        this.columnName = columnName;
        this.dataType = dataType;
        this.tableAlias = tableAlias;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public String getColumnName() {
        return columnName;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
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
