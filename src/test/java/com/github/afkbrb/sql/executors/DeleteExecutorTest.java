package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.SQLParseException;
import com.github.afkbrb.sql.TableManager;
import com.github.afkbrb.sql.model.Table;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DeleteExecutorTest extends ExecutorTest {

    @Test
    public void test() throws SQLParseException, SQLExecuteException, IOException {
        execute("create table student (id int, name string, age int, grade double)");
        execute("insert into student values (1, 'squanchy', 20, 90.01)");
        execute("insert into student values (2, 'abc', 24, 66.6)");
        execute("insert into student values (3, '渣渣辉', 24, 80)");
        execute("insert into student values (4, '张三', 20, 90)");
        Table student = TableManager.getInstance().getTable("student");
        assertEquals(4, student.getRowCount());
        execute("delete from student where id = 2333"); // 等于没有执行
        assertEquals(4, student.getRowCount());
        execute("delete from student where id = 1");
        assertEquals(3, student.getRowCount());
        execute("delete from student where name like '%三'");
        assertEquals(2, student.getRowCount());
        execute("delete from student where age = 24");
        assertEquals(0, student.getRowCount());
    }

}
