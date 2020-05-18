package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.SQLParseException;
import com.github.afkbrb.sql.TableManager;
import com.github.afkbrb.sql.model.Table;
import org.junit.Assert;
import org.junit.Test;

public class InsertExecutorTest extends ExecutorTest {

    @Test
    public void test() throws SQLParseException, SQLExecuteException {
        execute("create table student (id int, name string, age int, grade double)");
        execute("insert into student values (1, '渣渣辉', 20 + 2 * (1 + 1), 66.6)");
        execute("insert into student values (1 + 1, null, null, null)");
        execute("insert into student (id, grade) values (3, 66.6)");
        Table student = TableManager.getInstance().getTable("student");
        Assert.assertEquals("{" +
                        "\"tableName\": \"student\", " +
                        "\"columnCount\": 4, " +
                        "\"rowCount\": 3, " +
                        "\"columns\": [" +
                        "{\"index\": 0, \"name\": \"id\", \"type\": \"INT\", \"tableAlias\": \"student\"}, " +
                        "{\"index\": 1, \"name\": \"name\", \"type\": \"STRING\", \"tableAlias\": \"student\"}, " +
                        "{\"index\": 2, \"name\": \"age\", \"type\": \"INT\", \"tableAlias\": \"student\"}, " +
                        "{\"index\": 3, \"name\": \"grade\", \"type\": \"DOUBLE\", \"tableAlias\": \"student\"}" +
                        "], " +
                        "\"rows\": [" +
                        "[{\"type\": \"INT\", \"value\": 1}, {\"type\": \"STRING\", \"value\": \"渣渣辉\"}, {\"type\": \"INT\", \"value\": 24}, {\"type\": \"DOUBLE\", \"value\": 66.6}], " +
                        "[{\"type\": \"INT\", \"value\": 2}, {\"type\": \"NULL\", \"value\": null}, {\"type\": \"NULL\", \"value\": null}, {\"type\": \"NULL\", \"value\": null}], " +
                        "[{\"type\": \"INT\", \"value\": 3}, {\"type\": \"NULL\", \"value\": null}, {\"type\": \"NULL\", \"value\": null}, {\"type\": \"DOUBLE\", \"value\": 66.6}]" +
                        "]" +
                        "}",
                student.toString());
    }

    /**
     * 测试像不存在的 table 中插入数据。
     */
    @Test
    public void notExistTest() {
        Assert.assertThrows(SQLExecuteException.class, () -> execute("insert into student values (1, 'aaa', 20, 2333)"));
    }

    /**
     * 测试插入的列数和 value 个数不同。
     */
    @Test
    public void differentTest() throws SQLParseException, SQLExecuteException {
        execute("create table student (id int, name string, age int, grade double)");
        Assert.assertThrows(SQLExecuteException.class, () -> execute("insert into student values (1, 'aaa', 20, 2333, 'one more')"));
    }
}
