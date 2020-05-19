package com.github.afkbrb.sql.model;

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

    public static final Table DUMMY_TABLE;

    static {
        DUMMY_TABLE = new Table("dummy", Collections.emptyList());
        DUMMY_TABLE.addRow(new Row(Collections.emptyList()));
    }

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

    public List<Row> copyRows() {
        return new ArrayList<>(rows);
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return columns.size();
    }

    @Nullable
    public Cell getCell(int rowIndex, String columnName) {
        if (rowIndex >= 0 && rowIndex < rows.size()) {
            int columnIndex = schema.getColumnIndex(columnName);
            return rows.get(rowIndex).getCell(columnIndex);
        }
        return null;
    }

    @Nullable
    public Object getCellValue(int rowIndex, String columnName) {
        Cell cell = getCell(rowIndex, columnName);
        if (cell != null) {
            return cell.getTypedValue();
        }
        return null;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setRows(@NotNull List<Row> rows) {
        this.rows = Objects.requireNonNull(rows);
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
}
