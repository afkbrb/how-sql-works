package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.SQLParseException;
import com.github.afkbrb.sql.TableManager;
import com.github.afkbrb.sql.model.Table;
import com.github.afkbrb.sql.model.TypedValue;
import org.junit.Assert;
import org.junit.Test;

import static com.github.afkbrb.sql.model.DataType.*;

public class UpdateExecutorTest extends ExecutorTest {

    @Test
    public void test() throws SQLParseException, SQLExecuteException {
        execute("create table student (id int, name string, age int, grade double)");
        execute("insert into student values (1, 'squanchy', 20, 90.01)");
        execute("insert into student values (2, 'abc', 24, 66.6)");
        execute("insert into student values (3, '渣渣辉', 24, 80)");
        execute("insert into student values (4, '张三', 20, 90)");
        Table student = TableManager.getInstance().getTable("student");
        Assert.assertEquals(new TypedValue(DOUBLE, 66.6), student.getRow(1).getCell(3).getTypedValue());
        Assert.assertEquals(new TypedValue(DOUBLE, 80), student.getRow(2).getCell(3).getTypedValue());

        execute("update student set grade = 100 where age > 20 and age <= 24");
        Assert.assertEquals(new TypedValue(DOUBLE, 90.01), student.getRow(0).getCell(3).getTypedValue());
        Assert.assertEquals(new TypedValue(DOUBLE, 100), student.getRow(1).getCell(3).getTypedValue());
        Assert.assertEquals(new TypedValue(DOUBLE, 100), student.getRow(2).getCell(3).getTypedValue());
        Assert.assertEquals(new TypedValue(DOUBLE, 90), student.getRow(3).getCell(3).getTypedValue());

        execute("update student set age = 2333"); // 更新所有
        Assert.assertEquals(new TypedValue(INT, 2333), student.getRow(0).getCell(2).getTypedValue());
        Assert.assertEquals(new TypedValue(INT, 2333), student.getRow(1).getCell(2).getTypedValue());
        Assert.assertEquals(new TypedValue(INT, 2333), student.getRow(2).getCell(2).getTypedValue());
        Assert.assertEquals(new TypedValue(INT, 2333), student.getRow(3).getCell(2).getTypedValue());
    }
}
