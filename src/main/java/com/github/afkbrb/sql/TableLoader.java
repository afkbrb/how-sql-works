package com.github.afkbrb.sql;

import com.github.afkbrb.sql.model.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.github.afkbrb.sql.model.DataType.*;

/**
 * 从 csv 文件加载数据。
 */
public class TableLoader {

    private final File file;

    public TableLoader(String pathname) {
        this(new File(pathname));
    }

    public TableLoader(@NotNull File file) {
        this.file = Objects.requireNonNull(file);
    }

    public void loadTable() throws IOException {
        String tableName = file.getName();
        if (!tableName.endsWith(".csv")) {
            throw new IOException("invalid format of '" + tableName + "': expected a csv file");
        }
        tableName = tableName.substring(0, tableName.length() - 4);
        if (TableManager.getInstance().getTable(tableName) != null) {
            System.out.println("Warning: table '" + tableName + "' already exists, it will be overridden");
            TableManager.getInstance().removeTable(tableName);
        }
        CSVParser parser = new CSVParser(new FileReader(file), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        if (records.size() < 2) {
            throw new IOException("invalid format of '" + tableName + "': missing columns and/or types");
        }
        CSVRecord columnsRecord = records.get(0);
        int columnCount = columnsRecord.size();
        CSVRecord typesRecord = records.get(1);
        if (typesRecord.size() != columnCount) {
            throw new IOException("invalid format of '" + tableName + "': every record should have the same number of items");
        }

        List<Column> columns = new ArrayList<>();
        DataType[] dataTypes = new DataType[columnCount]; // 为了快速访问，提高加载速度
        for (int i = 0; i < columnCount; i++) {
            String columnName = columnsRecord.get(i);
            DataType dateType = getDateType(typesRecord.get(i));
            dataTypes[i] = dateType;
            Column column = new Column(i, columnName, dateType, tableName);
            columns.add(column);
        }

        Table table = new Table(tableName, columns);
        List<Row> rows = new ArrayList<>();
        for (int i = 2; i < records.size(); i++) {
            CSVRecord record = records.get(i);
            if (record.size() != columnCount)
                throw new IOException("invalid format of '" + tableName + "': every record should have the same number of items");
            List<Cell> cells = new ArrayList<>();
            for (int j = 0; j < columnCount; j++) {
                String valueStr = record.get(j);

                DataType dataType = dataTypes[j];
                Cell cell;
                if (valueStr.equals("")) {
                    cell = new Cell(TypedValue.NULL);
                } else if (dataType == INT) {
                    cell = new Cell(new TypedValue(INT, Integer.parseInt(valueStr)));
                } else if (dataType == DOUBLE) {
                    cell = new Cell(new TypedValue(DOUBLE, Double.parseDouble(valueStr)));
                } else {
                    cell = new Cell(new TypedValue(STRING, valueStr));
                }
                cells.add(cell);
            }
            rows.add(new Row(cells));
        }
        table.addRows(rows);

        TableManager.getInstance().addTable(table);
        parser.close();
        System.out.println("Successfully load table '" + tableName + "'");
    }


    private static DataType getDateType(String type) {
        String t = type.toUpperCase();
        if (t.equals("INT")) return INT;
        if (t.equals("STRING")) return STRING;
        if (t.equals("DOUBLE")) return DOUBLE;
        throw new IllegalArgumentException("invalid type '" + type + "'");
    }
}
