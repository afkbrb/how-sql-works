package com.github.afkbrb.sql;

import com.github.afkbrb.sql.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public class TableManager {

    private File dbDir = null;
    private final Map<String, Table> tableMap = new HashMap<>();

    public static final TableManager instance = new TableManager(); // 维护一个单例

    public static TableManager getInstance() {
        return instance;
    }

    public void addTable(Table table) throws IOException {
        tableMap.put(table.getTableName().toLowerCase(), table);
        if (dbDir == null) return;
        save(table);
    }

    public Table getTable(String tableName) {
        if (tableName == null) return null;
        return tableMap.get(tableName.toLowerCase());
    }

    public void removeTable(String tableName) throws IOException {
        if (tableName == null) return;
        tableMap.remove(tableName.toLowerCase());
        if (dbDir == null) return;
        File file = getTableFile(tableName);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("unable to delete file " + file.getCanonicalPath());
            }
        }
    }

    public List<Table> getTables() {
        return new ArrayList<>(tableMap.values());
    }

    public void reset() throws IOException {
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

    @Nullable
    public File getDbDir() {
        return dbDir;
    }

    public void save(@NotNull Table table) throws IOException {
        Objects.requireNonNull(table);
        if (dbDir == null) return;
        if (table.getColumnCount() == 0) return;
        File file = getTableFile(table.getTableName());
        Writer writer = new BufferedWriter(new FileWriter(file));

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

        writer.close();
    }


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

    private File getTableFile(String tableName) throws IOException {
        String pathname = dbDir.getCanonicalPath() + File.separator + tableName + ".csv";
        return new File(pathname);
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

    private TableManager() {
    }
}
