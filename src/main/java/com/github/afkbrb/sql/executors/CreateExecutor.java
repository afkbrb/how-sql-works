package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.TableManager;
import com.github.afkbrb.sql.ast.statements.CreateStatement;
import com.github.afkbrb.sql.model.Column;
import com.github.afkbrb.sql.model.Table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateExecutor extends Executor {



    /**
     * 根据 CreateStatement 创建一个表
     */
    public static void doCreate(CreateStatement createStatement) throws SQLExecuteException, IOException {
        String tableName = createStatement.getTableName();
        requireTableNotExists(tableName);

        List<Column> columnList = new ArrayList<>();
        List<CreateStatement.ColumnDefinition> columnDefinitionList = createStatement.getColumnDefinitionList();
        for (int i = 0; i < columnDefinitionList.size(); i++) {
            CreateStatement.ColumnDefinition columnDefinition = columnDefinitionList.get(i);
            Column column = new Column(i, columnDefinition.getColumnName(), columnDefinition.getColumnType(), tableName);
            columnList.add(column);
        }

        Table table = new Table(tableName, columnList);
        TableManager.getInstance().addTable(table);
    }
}
