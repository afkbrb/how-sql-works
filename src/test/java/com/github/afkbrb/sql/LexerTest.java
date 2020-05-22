package com.github.afkbrb.sql;

import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

public class LexerTest {

    @Test
    public void apiTest() throws SQLParseException {
        String test = "select * from table1;";
        Lexer lexer = new Lexer(new StringReader(test));
        Assert.assertEquals("SELECT: SELECT", lexer.current().toString());
        Assert.assertEquals("SELECT: SELECT", lexer.current().toString());
        Assert.assertEquals("SELECT: SELECT", lexer.lookAhead(0).toString());
        Assert.assertEquals("MULT: *", lexer.peek().toString());
        Assert.assertEquals("MULT: *", lexer.peek().toString());
        Assert.assertEquals("MULT: *", lexer.lookAhead(1).toString());
        Assert.assertEquals("IDENTIFIER: table1", lexer.lookAhead(3).toString());
        Assert.assertEquals("SEMICOLON: ;", lexer.lookAhead(4).toString());
        Assert.assertEquals("EOF: ", lexer.lookAhead(2333).toString());
        Assert.assertEquals("SELECT: SELECT", lexer.consume().toString());
        Assert.assertEquals("MULT: *", lexer.current().toString());
        Assert.assertEquals("FROM: FROM", lexer.peek().toString());
    }

    @Test
    public void allTest() throws SQLParseException {
        String expected = "CREATE: CREATE\n" +
                "TABLE: TABLE\n" +
                "IDENTIFIER: student\n" +
                "OPEN_PAR: (\n" +
                "IDENTIFIER: id\n" +
                "INT: INT\n" +
                "COMMA: ,\n" +
                "IDENTIFIER: name\n" +
                "STRING: STRING\n" +
                "COMMA: ,\n" +
                "IDENTIFIER: age\n" +
                "INT: INT\n" +
                "CLOSE_PAR: )\n" +
                "SEMICOLON: ;\n" +
                "INSERT: INSERT\n" +
                "INTO: INTO\n" +
                "IDENTIFIER: student\n" +
                "OPEN_PAR: (\n" +
                "IDENTIFIER: id\n" +
                "COMMA: ,\n" +
                "IDENTIFIER: name\n" +
                "COMMA: ,\n" +
                "IDENTIFIER: age\n" +
                "CLOSE_PAR: )\n" +
                "VALUES: VALUES\n" +
                "OPEN_PAR: (\n" +
                "INT_LITERAL: 1\n" +
                "COMMA: ,\n" +
                "STRING_LITERAL: 小明\n" +
                "COMMA: ,\n" +
                "INT_LITERAL: 20\n" +
                "CLOSE_PAR: )\n" +
                "SEMICOLON: ;\n" +
                "SELECT: SELECT\n" +
                "IDENTIFIER: id\n" +
                "COMMA: ,\n" +
                "IDENTIFIER: name\n" +
                "AS: AS\n" +
                "IDENTIFIER: student_name\n" +
                "COMMA: ,\n" +
                "IDENTIFIER: sum\n" +
                "OPEN_PAR: (\n" +
                "IDENTIFIER: age\n" +
                "CLOSE_PAR: )\n" +
                "FROM: FROM\n" +
                "IDENTIFIER: student\n" +
                "WHERE: WHERE\n" +
                "IDENTIFIER: student\n" +
                "DOT: .\n" +
                "IDENTIFIER: age\n" +
                "EQ: =\n" +
                "INT_LITERAL: 20\n" +
                "AND: AND\n" +
                "IDENTIFIER: f\n" +
                "OPEN_PAR: (\n" +
                "CLOSE_PAR: )\n" +
                "GROUP: GROUP\n" +
                "BY: BY\n" +
                "IDENTIFIER: foo\n" +
                "HAVING: HAVING\n" +
                "IDENTIFIER: bar\n" +
                "ORDER: ORDER\n" +
                "BY: BY\n" +
                "IDENTIFIER: something\n" +
                "LIMIT: LIMIT\n" +
                "INT_LITERAL: 10\n" +
                "OFFSET: OFFSET\n" +
                "INT_LITERAL: 2333\n" +
                "INT_LITERAL: 1\n" +
                "ADD: +\n" +
                "INT_LITERAL: 2\n" +
                "MULT: *\n" +
                "INT_LITERAL: 3\n" +
                "DIV: /\n" +
                "INT_LITERAL: 4\n" +
                "MINUS: -\n" +
                "INT_LITERAL: 5\n" +
                "ADD: +\n" +
                "OPEN_PAR: (\n" +
                "DOUBLE_LITERAL: 666.000\n" +
                "ADD: +\n" +
                "DOUBLE_LITERAL: 0.0\n" +
                "ADD: +\n" +
                "INT_LITERAL: 11111\n" +
                "CLOSE_PAR: )\n" +
                "IDENTIFIER: a\n" +
                "DOT: .\n" +
                "IDENTIFIER: b\n" +
                "DOT: .\n" +
                "IDENTIFIER: c\n" +
                "DOT: .\n" +
                "IDENTIFIER: d\n" +
                "IDENTIFIER: a\n" +
                "DOT: .\n" +
                "MULT: *\n" +
                "NE: !=\n" +
                "STRING_LITERAL: \\\n" +
                "STRING_LITERAL: 'escape'\n";

        String statements = "create table student(id int, name string, age int);\n" +
                "insert into student (id, name, age) values(1, '小明', 20);\n" +
                "select id, name As student_name, sum(age) from student where student.age = 20 and f() group by foo having bar order by something limit 10 offset 2333\n" +
                "1+2 * 3 / 4 - 5 + (666.000 + 0.0 + 11111) a.b.c.d  a.*  != '\\\\' '\\'escape\\''";

        Lexer lexer = new Lexer(new StringReader(statements));
        StringBuilder sb = new StringBuilder();
        for (Token token = lexer.consume(); token.getType() != TokenType.EOF; token = lexer.consume()) {
            sb.append(token.getType().name()).append(": ").append(token.getText()).append("\n");
        }
        Assert.assertEquals(expected, sb.toString());
    }

    @Test
    public void commentTest() throws SQLParseException {
        String expected = "SELECT: SELECT\n" +
                "INT_LITERAL: 1\n" +
                "ADD: +\n" +
                "INT_LITERAL: 1\n" +
                "SEMICOLON: ;\n" +
                "STRING_LITERAL: -- this single line comment should be read\n" +
                "STRING_LITERAL: /* this multi lines comment should be read, too */\n";
        String statements = "-- fjdlsjfldsjfldsjfldsjfkd\n" +
                " -- fjdlksjfsl select * from test; \n " +
                "select 1 + 1; '-- this single line comment should be read'" +
                "'/* this multi lines comment should be read, too */'" +
                "/************ / */" +
                " /*--\n\n\n\n*/";
        Lexer lexer = new Lexer(new StringReader(statements));
        StringBuilder sb = new StringBuilder();
        for (Token token = lexer.consume(); token.getType() != TokenType.EOF; token = lexer.consume()) {
            sb.append(token.getType().name()).append(": ").append(token.getText()).append("\n");
        }
        Assert.assertEquals(expected, sb.toString());
    }
}
