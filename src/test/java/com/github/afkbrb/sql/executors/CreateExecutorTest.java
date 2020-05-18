package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.SQLParseException;
import com.github.afkbrb.sql.TableManager;
import com.github.afkbrb.sql.model.Table;
import org.junit.Assert;
import org.junit.Test;


public class CreateExecutorTest extends ExecutorTest {

    @Test
    public void test() throws SQLParseException, SQLExecuteException {
        ExecutorTest.execute("create table student (id int, name string, age int, grade double)");
        Table student = TableManager.getInstance().getTable("student");
        Assert.assertEquals("{" +
                "\"tableName\": \"student\", " +
                "\"columnCount\": 4, " +
                "\"rowCount\": 0, " +
                "\"columns\": [" +
                "{\"index\": 0, \"name\": \"id\", \"type\": \"INT\", \"tableAlias\": \"student\"}, " +
                "{\"index\": 1, \"name\": \"name\", \"type\": \"STRING\", \"tableAlias\": \"student\"}, " +
                "{\"index\": 2, \"name\": \"age\", \"type\": \"INT\", \"tableAlias\": \"student\"}, " +
                "{\"index\": 3, \"name\": \"grade\", \"type\": \"DOUBLE\", \"tableAlias\": \"student\"}" +
                "], " +
                "\"rows\": []" +
                "}", student.toString());
    }
}
