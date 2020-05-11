package com.github.afkbrb.sql;

import com.github.afkbrb.sql.ast.statements.Statement;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

public class ParserTest {

    @Test
    public void createTest() throws ParseException {
        String statement = "create table table1 (id int, name text, age int, grade double);";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("CREATE TABLE table1(id INT, name TEXT, age INT, grade DOUBLE);", viewer.getASTString());
    }

    @Test
    public void dropTest() throws ParseException {
        String statement = "drop table table1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("DROP TABLE table1;", viewer.getASTString());
    }

    @Test
    public void insertTest() throws ParseException {
        String statement = "insert into table1 (id, name, age, grade) values (1 + 1 * 1 / 1 - 1, '渣渣辉', 2333 + 6666, 2333.6666);";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("INSERT INTO table1 (id, name, age, grade) VALUES (((1 + ((1 * 1) / 1)) - 1), '渣渣辉', (2333 + 6666), 2333.6666);", viewer.getASTString());
    }

    @Test
    public void updateTest() throws ParseException {
        String statement = "update table1 set id = 1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("UPDATE table1 SET id = 1;", viewer.getASTString());
    }

    @Test
    public void updateWhereTest() throws ParseException {
        String statement = "update table1 set id = 1 + 1, name = 'squanchy' where 1 between 2 and 3 and 1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("UPDATE table1 SET id = (1 + 1), name = 'squanchy' WHERE ((1 BETWEEN 2 AND 3) AND 1);", viewer.getASTString());
    }

    @Test
    public void deleteTest() throws ParseException {
        String statement = "delete from table1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("DELETE FROM table1;", viewer.getASTString());
    }

    @Test
    public void deleteWhereTest() throws ParseException {
        String statement = "delete from table1 where 1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("DELETE FROM table1 WHERE 1;", viewer.getASTString());
    }

    @Test
    public void selectTest() throws ParseException {
        String statement = "select * from table1 as table1_alias where id > 1 group by id having 1 order by xxx asc limit 666 offset 2333;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT * FROM table1 AS table1_alias WHERE (id > 1) GROUP BY id HAVING 1 ORDER BY xxx ASC LIMIT 666 OFFSET 2333;", viewer.getASTString());
    }

    @Test
    public void selectAllTest() throws ParseException {
        String statement = "select all * from test1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT ALL * FROM test1;", viewer.getASTString());
    }

    @Test
    public void selectDistinctTest() throws ParseException {
        String statement = "select distinct * as xxx, sum(age) as sum, table1.* from table1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT DISTINCT * AS xxx, sum(age) AS sum, table1.* FROM table1;", viewer.getASTString());
    }

    @Test
    public void subQueryTest() throws ParseException {
        String statement = "select * from (select * from (select * from (select * from table1)));";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT * FROM (SELECT * FROM (SELECT * FROM (SELECT * FROM table1)));", viewer.getASTString());
    }

    @Test
    public void joinTest1() throws ParseException {
        String statement = "select * from (table1 left join (table2 inner join table3) as a) as b;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT * FROM (((table1 LEFT JOIN (((table2 INNER JOIN table3)) AS a))) AS b);", viewer.getASTString());
    }

    @Test
    public void joinTest2() throws ParseException {
        String statement = "select * from table1 left join table2 inner join (select * from (select * from table3) as _from_table3) as _derived_table;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT * FROM ((table1 LEFT JOIN table2) INNER JOIN ((SELECT * FROM ((SELECT * FROM table3) AS _from_table3)) AS _derived_table));", viewer.getASTString());
    }

    @Test
    public void joinTest3() throws ParseException {
        String statement = "select * from table1 left join table1 on 1 + 1 = 2 inner join table3 on 1 between 2 and 3;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT * FROM ((table1 LEFT JOIN table1 ON ((1 + 1) = 2)) INNER JOIN table3 ON (1 BETWEEN 2 AND 3));", viewer.getASTString());
    }

    @Test
    public void expressionPrecedenceTest1() throws ParseException {
        String statement = "select 1 + 1 + 1 * (2 + var1) / foo(bar);";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT ((1 + 1) + ((1 * (2 + var1)) / foo(bar)));", viewer.getASTString());
    }

    @Test
    public void expressionPrecedenceTest2() throws ParseException {
        String statement = "select 1 < 2 and 2 between 1 and 3 or (some_condition or another_condition) and name not like '%小_%';";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT (((1 < 2) AND (2 BETWEEN 1 AND 3)) OR ((some_condition OR another_condition) AND (name NOT LIKE '%小_%')));", viewer.getASTString());
    }

    @Test
    public void expressionPrecedenceTest3() throws ParseException {
        String statement = "select NOT condition1 AND NOT condition2 OR NOT NOT NOT condition3 AND condition4;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT (((NOT condition1) AND (NOT condition2)) OR ((NOT (NOT (NOT condition3))) AND condition4));", viewer.getASTString());
    }

    @Test
    public void expressionPrecedenceTest4() throws ParseException {
        String statement = "select 1 + -1 * 2333 + -(1 * 2333) + --++1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT (((1 + ((-1) * 2333)) + (-(1 * 2333))) + (-(-(+(+1)))));", viewer.getASTString());
    }

    @Test
    public void expressionFuncCallTest() throws ParseException {
        String statement = "select f1(f2(f3(), f4(), f5(a, b, c, 2333, '烫烫烫')));";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT f1(f2(f3(), f4(), f5(a, b, c, 2333, '烫烫烫')));", viewer.getASTString());
    }

    @Test
    public void expressionCmpTest() throws ParseException {
        String statement = "select 1 < 1 OR 1 <= 1 Or 1 = 1 oR 1 >= 1 or 1 > 1 OR 1 != 1;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT ((((((1 < 1) OR (1 <= 1)) OR (1 = 1)) OR (1 >= 1)) OR (1 > 1)) OR (1 != 1));", viewer.toString()); // toString 实际调用 getASTString
    }

    @Test
    public void expressionMiscTest() throws ParseException {
        String statement = "select 1 in (1, 2, 3) OR 1 not in (4, 5, 6) OR 1 in (select * from table1) OR 1 not in (select * from table2) OR aaa like bbb OR foo is null OR bar is not null;";
        Lexer lexer = new Lexer(new StringReader(statement));
        Parser parser = new Parser(lexer);
        List<Statement> statementList = parser.statementList();
        ASTStringViewer viewer = new ASTStringViewer(statementList.get(0));
        Assert.assertEquals("SELECT (((((((1 IN (1, 2, 3)) OR (1 NOT IN (4, 5, 6))) OR (1 IN (SELECT * FROM table1))) OR (1 NOT IN (SELECT * FROM table2))) OR (aaa LIKE bbb)) OR (foo IS NULL)) OR (bar IS NOT NULL));", viewer.getASTString());
    }
}
