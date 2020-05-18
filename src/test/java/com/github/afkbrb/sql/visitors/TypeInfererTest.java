package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.Lexer;
import com.github.afkbrb.sql.Parser;
import com.github.afkbrb.sql.SQLParseException;
import com.github.afkbrb.sql.model.Column;
import com.github.afkbrb.sql.model.DataType;
import com.github.afkbrb.sql.model.Schema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static com.github.afkbrb.sql.model.DataType.*;

public class TypeInfererTest {

    private TypeInferer inferer;

    @Before
    public void setup() {
        List<Column> columnList = new ArrayList<>();
        columnList.add(new Column(0, "id", INT, "test_table"));
        columnList.add(new Column(1, "money", DOUBLE, "test_table"));
        columnList.add(new Column(2, "name", STRING, "test_table"));
        columnList.add(new Column(3, "name", STRING, "another_table"));
        Schema schema = new Schema(columnList);
        inferer = new TypeInferer(schema);
    }

    @Test
    public void allTest() throws SQLParseException {
        assertType(INT, "1 + 1");
        assertType(DOUBLE, "1 + 1.0");
        assertType(INT, "-1");
        assertType(DOUBLE, "-1.0");
        assertType(INT, "'a' like 'b'");
        assertType(STRING, "'squanchy'");
        assertType(INT, "id");
        assertType(DOUBLE, "money");
        assertType(STRING, "test_table.name");
        assertType(STRING, "another_table.name");
        assertType(ERROR, "not_exist");

        assertType(DOUBLE, "max(age)");
        assertType(DOUBLE, "max(foo() + bar() + 1 * 1 + id)");
        assertType(ERROR, "not_a_function()");
    }

    private void assertType(DataType type, String expression) throws SQLParseException {
        Assert.assertEquals(type, inferer.infer(new Parser(new Lexer(new StringReader(expression))).expression()));
    }

}
