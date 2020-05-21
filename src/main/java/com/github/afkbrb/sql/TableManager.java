package com.github.afkbrb.sql;

import com.github.afkbrb.sql.model.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableManager {

    private final Map<String, Table> tableMap = new HashMap<>();

    public static final TableManager instance = new TableManager(); // 维护一个单例

    private TableManager() {}

    public void addTable(Table table) {
        tableMap.put(table.getTableName().toLowerCase(), table);
    }

    public Table getTable(String tableName) {
        if (tableName == null) return null;
        return tableMap.get(tableName.toLowerCase());
    }

    public void removeTable(String tableName) {
        if (tableName == null) return;
        tableMap.remove(tableName.toLowerCase());
    }

    public static TableManager getInstance() {
        return instance;
    }

    public List<Table> getTables() {
        return new ArrayList<>(tableMap.values());
    }

    public void reset() {
        tableMap.clear();
    }

}
