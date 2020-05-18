package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.SQLParseException;
import com.github.afkbrb.sql.TableManager;
import org.junit.Assert;
import org.junit.Test;

public class DropExecutorTest extends ExecutorTest {

    @Test
    public void test() throws SQLExecuteException, SQLParseException {
        execute("create table student (id int, name string, age int, grade double)");
        Assert.assertNotNull(TableManager.getInstance().getTable("student"));
        execute("drop table student");
        Assert.assertNull(TableManager.getInstance().getTable("student"));
    }
}
