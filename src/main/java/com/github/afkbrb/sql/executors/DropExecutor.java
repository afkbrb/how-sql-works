package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.TableManager;
import com.github.afkbrb.sql.ast.statements.DropStatement;

import java.io.IOException;

public class DropExecutor extends Executor {

    public static void doDrop(DropStatement dropStatement) throws SQLExecuteException, IOException {
        requireTableExists(dropStatement.getTableName());
        TableManager.getInstance().removeTable(dropStatement.getTableName());
    }
}
