package com.github.afkbrb.sql;

import com.github.afkbrb.sql.model.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.github.afkbrb.sql.model.DataType.*;

public class TableManager {

    private File dbDir = null;
    private final Map<String, Table> tableMap = new HashMap<>();

    public static final TableManager instance = new TableManager(); // 维护一个单例

    public static TableManager getInstance() {
        return instance;
    }

    public void addTable(@NotNull Table table) throws IOException {
        Objects.requireNonNull(table);
        tableMap.put(table.getTableName().toLowerCase(), table);
        if (dbDir == null) return;
        saveTable(table);
    }

    @Nullable
    public Table getTable(String tableName) {
        if (tableName == null) return null;
        return tableMap.get(tableName.toLowerCase());
    }

    public List<Table> getTables() {
        return new ArrayList<>(tableMap.values());
    }

    public void removeTable(String tableName) throws IOException {
        if (tableName == null) return;
        tableMap.remove(tableName.toLowerCase());
        if (dbDir == null) return;
        String pathname = dbDir.getCanonicalPath() + File.separator + tableName + ".csv";
        File file = new File(pathname);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("unable to delete file " + file.getCanonicalPath());
            }
        }
    }

    public void clear() throws IOException {
        tableMap.clear();
        if (dbDir == null) return;
        File[] files = dbDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".csv")) {
                    if (!file.delete()) {
                        throw new IOException("unable to delete file " + file.getCanonicalPath());
                    }
                }
            }
        }
    }

    @Nullable
    public File getDbDir() {
        return dbDir;
    }

    public void setDbDir(@NotNull File dbDir) throws IOException {
        Objects.requireNonNull(dbDir);
        if (!dbDir.canRead()) {
            throw new IOException("Cannot read db directory '" + dbDir + "'");
        }
        if (!dbDir.isDirectory()) {
            throw new IOException("'" + dbDir + "' is not a directory");
        }
        if (this.dbDir == null) {
            System.out.printf("Set db directory to '%s'\n", dbDir.getCanonicalPath());
        } else {
            System.out.printf("Switch db directory from '%s' to '%s'\n", this.dbDir.getCanonicalPath(), dbDir.getCanonicalPath());
        }
        this.dbDir = dbDir;
    }


    /**
     * 将 table 以 UTF-8 编码保存到 csv 文件中。
     */
    public void saveTable(@NotNull Table table) throws IOException {
        if (dbDir == null) return;
        TableSaver tableSaver = new TableSaver(dbDir, Objects.requireNonNull(table));
        tableSaver.saveTable();
    }


    /**
     * 根据 UTF-8 编码的 csv 文件生成 table。
     * 懒加载策略更好些，但此处简单地一次性全加载了。
     */
    public void loadTables() throws IOException {
        tableMap.clear();
        File[] files = dbDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".csv")) {
                    new TableLoader(file).loadTable();
                }
            }
        }
    }

    private TableManager() {
    }

    private static class TableLoader {

        private final File file;

        public TableLoader(@NotNull File file) {
            this.file = Objects.requireNonNull(file);
        }

        public void loadTable() throws IOException {
            String filename = file.getName();
            if (!filename.endsWith(".csv")) {
                throw new IOException("invalid format of '" + filename + "': expected a csv file");
            }
            String tableName = filename.substring(0, filename.length() - 4);
            if (TableManager.getInstance().getTable(tableName) != null) {
                System.out.println("Warning: table '" + tableName + "' already exists, it will be overridden");
                TableManager.getInstance().removeTable(tableName);
            }

            try (CSVParser parser = new CSVParser(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), CSVFormat.DEFAULT)) {
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
                System.out.println("Successfully load table '" + tableName + "'");
            }
        }


        private static DataType getDateType(String type) {
            String t = type.toUpperCase();
            if (t.equals("INT")) return INT;
            if (t.equals("STRING")) return STRING;
            if (t.equals("DOUBLE")) return DOUBLE;
            throw new IllegalArgumentException("invalid type '" + type + "'");
        }
    }

    private static class TableSaver {

        private final File dbDir;
        private final Table table;

        public TableSaver(@NotNull File dbDir, @NotNull Table table) {
            this.dbDir = Objects.requireNonNull(dbDir);
            this.table = Objects.requireNonNull(table);
        }

        public void saveTable() throws IOException {
            if (table.getColumnCount() == 0) return;
            String pathname = dbDir.getCanonicalPath() + File.separator + table.getTableName() + ".csv";
            File file = new File(pathname);
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8))) {
                List<Column> columns = table.getColumns();
                writer.write(csvNormalize(columns.get(0).getColumnName()));
                for (int i = 1; i < columns.size(); i++) {
                    writer.write(",");
                    writer.write(csvNormalize(columns.get(i).getColumnName()));
                }
                writer.write("\n"); // TODO: \r\n?

                writer.write(csvNormalize(columns.get(0).getDataType().name()));
                for (int i = 1; i < columns.size(); i++) {
                    writer.write(",");
                    writer.write(csvNormalize(columns.get(i).getDataType().name()));
                }
                writer.write("\n");

                for (Row row : table.getRows()) {
                    List<Cell> cells = row.getCells();
                    writer.write(normalizeTypedData(cells.get(0).getTypedValue()));
                    for (int i = 1; i < cells.size(); i++) {
                        writer.write(",");
                        writer.write(normalizeTypedData(cells.get(i).getTypedValue()));
                    }
                    writer.write("\n");
                }
            }
        }

        private static String normalizeTypedData(TypedValue typedValue) {
            switch (typedValue.getDataType()) {
                case INT:
                case DOUBLE:
                    return typedValue.getValue().toString();
                case STRING:
                    return csvNormalize((String) typedValue.getValue());
                case NULL:
                    return "";
                case ERROR:
                    throw new IllegalStateException("unexpected error data a table: " + typedValue.getValue());
                default:
                    throw new IllegalStateException("bug");
            }
        }

        private static String csvNormalize(String str) {
            String replace = str.replace("\"", "\"\"");
            return "\"" + replace + "\"";
        }
    }
}
