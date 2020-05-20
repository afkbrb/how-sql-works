package com.github.afkbrb.sql.model;

import com.github.afkbrb.sql.SQLExecuteException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 数据表的 Java 表示。
 */
public class Table {

    private String tableName;
    private List<Column> columns;
    private Schema schema;
    private List<Row> rows = new ArrayList<>();

    public Table(@Nullable String tableName, @NotNull List<Column> columns) {
        this.tableName = tableName;
        this.columns = Objects.requireNonNull(columns);
        schema = new Schema(columns);
    }

    public Schema getSchema() {
        return schema;
    }

    public void addRow(@NotNull Row row) {
        // TODO: 检查约束
        rows.add(Objects.requireNonNull(row));
    }

    public void addRows(@NotNull List<Row> rows) {
        Objects.requireNonNull(rows);
        for (Row row : rows) {
            addRow(row);
        }
    }

    @Nullable
    public Row getRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < rows.size()) {
            return rows.get(rowIndex);
        }
        return null;
    }

    public List<Row> getRows() {
        return rows;
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return columns.size();
    }

    @Nullable
    public Cell getCell(int rowIndex, String columnName) throws SQLExecuteException {
        if (rowIndex >= 0 && rowIndex < rows.size()) {
            Column column = schema.getColumn(columnName);
            if (column != null) {
                return rows.get(rowIndex).getCell(column.getColumnIndex());
            }
        }
        return null;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public String getTableName() {
        return tableName;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"tableName\": ").append("\"").append(tableName).append("\"").append(", ");
        sb.append("\"columnCount\": ").append(columns.size()).append(", ");
        sb.append("\"rowCount\": ").append(rows.size()).append(", ");

        sb.append("\"columns\": [");
        boolean first = true;
        for (Column column : columns) {
            if (!first) sb.append(", ");
            sb.append(column);
            first = false;
        }
        sb.append("], ");

        sb.append("\"rows\": [");
        first = true;
        for (Row row : rows) {
            if (!first) sb.append(", ");
            sb.append(row);
            first = false;
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    /**
     * dummyTable 作为中间表使用，其内部的 rows 可能会被 where 过滤掉，所以每次都得返回一个新实例。
     */
    public static Table dummyTable() {
        Table dummy = new Table("dummy", Collections.emptyList());
        dummy.addRow(new Row(Collections.emptyList()));
        return dummy;
    }
}
