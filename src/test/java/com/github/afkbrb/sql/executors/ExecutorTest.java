package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.*;
import org.junit.Before;

import java.io.IOException;
import java.io.StringReader;

public class ExecutorTest {

    /**
     * 每次执行测试前都先清除前面的记录。
     */
    @Before
    public void reset() throws IOException {
        TableManager.getInstance().reset();
    }

    protected static void execute(String statement) throws SQLParseException, SQLExecuteException, IOException {
        statement = statement.trim();
        Parser parser = new Parser(new Lexer(new StringReader(statement)));
        String action = statement.split("\\s+")[0].toLowerCase();
        switch (action) {
            case "create":
                CreateExecutor.doCreate(parser.createTableStatement());
                break;
            case "drop":
                DropExecutor.doDrop(parser.dropTableStatement());
                break;
            case "insert":
                InsertExecutor.doInsert(parser.insertStatement());
                break;
            case "update":
                UpdateExecutor.doUpdate(parser.updateStatement());
                break;
            case "delete":
                DeleteExecutor.doDelete(parser.deleteStatement());
                break;
            case "select":
                new SelectExecutor().doSelect(parser.selectStatement());
                break;
            default:
                throw new IllegalArgumentException("unexpected action " + action);
        }
    }
}
