package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.Lexer;
import com.github.afkbrb.sql.Parser;
import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.SQLParseException;
import com.github.afkbrb.sql.model.Table;
import org.junit.Test;

import java.io.StringReader;

public class SelectExecutorTest extends ExecutorTest {

    private static Table select(String selectStatement) throws SQLParseException, SQLExecuteException {
        selectStatement = selectStatement.trim();
        Parser parser = new Parser(new Lexer(new StringReader(selectStatement)));
        String action = selectStatement.split(" ")[0].toLowerCase();
        if (!action.equalsIgnoreCase("select")) throw new IllegalArgumentException("expected select, but got " + action);
        return SelectExecutor.doSelect(parser.selectStatement());
    }

    @Test
    public void test() throws SQLParseException, SQLExecuteException {
        execute("create table student (id int, name string, age int, grade double)");
        execute("insert into student values (1, 'squanchy', 20, 90.01)");
        execute("insert into student values (2, 'abc', 24, 66.6)");
        execute("insert into student values (3, '渣渣辉', 24, 80)");
    }
}
