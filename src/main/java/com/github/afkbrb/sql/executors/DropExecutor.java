package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.TableManager;
import com.github.afkbrb.sql.ast.statements.DropStatement;

public class DropExecutor extends Executor {

    public static void doDrop(DropStatement dropStatement) throws SQLExecuteException {
        requireTableExists(dropStatement.getTableName());
        TableManager.getInstance().removeTable(dropStatement.getTableName());
    }
}
