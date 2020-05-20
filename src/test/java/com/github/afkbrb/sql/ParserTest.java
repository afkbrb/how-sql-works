package com.github.afkbrb.sql;

import com.github.afkbrb.sql.ast.statements.Statement;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

public class ParserTest {

    @Test
    public void createTest() throws SQLParseException {
        String statement = "create table table1 (id int, name string, age int, grade double);";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("CREATE TABLE table1(id INT, name STRING, age INT, grade DOUBLE);", statementList.get(0).toString());
    }

    @Test
    public void dropTest() throws SQLParseException {
        String statement = "drop table table1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("DROP TABLE table1;", statementList.get(0).toString());
    }

    @Test
    public void insertTest() throws SQLParseException {
        String statement = "insert into table1 (id, name, age, grade) values (1 + 1 * 1 / 1 - 1, '渣渣辉', 2333 + 6666, 2333.6666);";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("INSERT INTO table1 (id, name, age, grade) VALUES (((1 + ((1 * 1) / 1)) - 1), '渣渣辉', (2333 + 6666), 2333.6666);", statementList.get(0).toString());
    }

    @Test
    public void updateTest() throws SQLParseException {
        String statement = "update table1 set id = 1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("UPDATE table1 SET id = 1;", statementList.get(0).toString());
    }

    @Test
    public void updateWhereTest() throws SQLParseException {
        String statement = "update table1 set id = 1 + 1, name = 'squanchy' where 1 between 2 and 3 and 1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("UPDATE table1 SET id = (1 + 1), name = 'squanchy' WHERE ((1 BETWEEN 2 AND 3) AND 1);", statementList.get(0).toString());
    }

    @Test
    public void deleteTest() throws SQLParseException {
        String statement = "delete from table1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("DELETE FROM table1;", statementList.get(0).toString());
    }

    @Test
    public void deleteWhereTest() throws SQLParseException {
        String statement = "delete from table1 where 1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("DELETE FROM table1 WHERE 1;", statementList.get(0).toString());
    }

    @Test
    public void selectTest() throws SQLParseException {
        String statement = "select * from table1 as table1_alias where id > 1 group by id, name having 1 order by xxx, yyy asc limit 666 offset 2333;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("SELECT * FROM table1 AS table1_alias WHERE (id > 1) GROUP BY id, name HAVING 1 ORDER BY xxx ASC, yyy ASC LIMIT 666 OFFSET 2333;", statementList.get(0).toString());
    }

    @Test
    public void subQueryTest() throws SQLParseException {
        String statement = "select * from (select * from (select * from (select * from table1) as t3) as t2) t1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("SELECT * FROM ((SELECT * FROM ((SELECT * FROM ((SELECT * FROM table1) AS t3)) AS t2)) AS t1);", statementList.get(0).toString());
    }

    @Test
    public void joinTest1() throws SQLParseException {
        String statement = "select * from table1 left join (table2 inner join table3);";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("SELECT * FROM (table1 LEFT JOIN (table2 INNER JOIN table3));", statementList.get(0).toString());
    }

    @Test
    public void joinTest2() throws SQLParseException {
        String statement = "select * from table1 left join table2 inner join (select * from (select * from table3) as _from_table3) as _derived_table;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("SELECT * FROM ((table1 LEFT JOIN table2) INNER JOIN ((SELECT * FROM ((SELECT * FROM table3) AS _from_table3)) AS _derived_table));", statementList.get(0).toString());
    }

    @Test
    public void joinTest3() throws SQLParseException {
        String statement = "select * from table1 left join table1 on 1 + 1 = 2 inner join table3 on 1 between 2 and 3;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("SELECT * FROM ((table1 LEFT JOIN table1 ON ((1 + 1) = 2)) INNER JOIN table3 ON (1 BETWEEN 2 AND 3));", statementList.get(0).toString());
    }

    @Test
    public void expressionPrecedenceTest1() throws SQLParseException {
        String statement = "select 1 + 1 + 1 * (2 + var1) / foo(bar);";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("SELECT ((1 + 1) + ((1 * (2 + var1)) / foo(bar)));", statementList.get(0).toString());
    }

    @Test
    public void expressionPrecedenceTest2() throws SQLParseException {
        String statement = "select 1 < 2 and 2 between 1 and 3 or (some_condition or another_condition) and name not like '%小_%';";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("SELECT (((1 < 2) AND (2 BETWEEN 1 AND 3)) OR ((some_condition OR another_condition) AND (name NOT LIKE '%小_%')));", statementList.get(0).toString());
    }

    @Test
    public void expressionPrecedenceTest3() throws SQLParseException {
        String statement = "select NOT condition1 AND NOT condition2 OR NOT NOT NOT condition3 AND condition4;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("SELECT (((NOT condition1) AND (NOT condition2)) OR ((NOT (NOT (NOT condition3))) AND condition4));", statementList.get(0).toString());
    }

    @Test
    public void expressionPrecedenceTest4() throws SQLParseException {
        String statement = "select 1 + -1 * 2333 + -(1 * 2333) + --++1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("SELECT (((1 + ((-1) * 2333)) + (-(1 * 2333))) + (-(-(+(+1)))));", statementList.get(0).toString());
    }

    @Test
    public void expressionFuncCallTest() throws SQLParseException {
        String statement = "select f1(f2(f3(), f4(), f5(a, b, c, 2333, '烫烫烫')));";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("SELECT f1(f2(f3(), f4(), f5(a, b, c, 2333, '烫烫烫')));", statementList.get(0).toString());
    }

    @Test
    public void expressionCmpTest() throws SQLParseException {
        String statement = "select 1 < 1 OR 1 <= 1 Or 1 = 1 oR 1 >= 1 or 1 > 1 OR 1 != 1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("SELECT ((((((1 < 1) OR (1 <= 1)) OR (1 = 1)) OR (1 >= 1)) OR (1 > 1)) OR (1 != 1));", statementList.get(0).toString());
    }

    @Test
    public void expressionMiscTest() throws SQLParseException {
        String statement = "select 1 in (1, 2, 3) OR 1 not in (4, 5, 6) OR 1 in (select * from table1) OR 1 not in (select * from table2) OR aaa like bbb OR foo is null OR bar is not null;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        Assert.assertEquals("SELECT (((((((1 IN (1, 2, 3)) OR (1 NOT IN (4, 5, 6))) OR (1 IN (SELECT * FROM table1))) OR (1 NOT IN (SELECT * FROM table2))) OR (aaa LIKE bbb)) OR (foo IS NULL)) OR (bar IS NOT NULL));", statementList.get(0).toString());
    }
}
