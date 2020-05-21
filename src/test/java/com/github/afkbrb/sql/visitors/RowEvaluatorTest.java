package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.Lexer;
import com.github.afkbrb.sql.Parser;
import com.github.afkbrb.sql.SQLParseException;
import com.github.afkbrb.sql.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static com.github.afkbrb.sql.model.DataType.*;

public class RowEvaluatorTest {

    private RowEvaluator evaluator;

    @Before
    public void setup() {
        List<Column> columnList = new ArrayList<>();
        columnList.add(new Column(0, "id", INT, "test_table"));
        columnList.add(new Column(1, "name", STRING, "test_table"));
        columnList.add(new Column(2, "grade", DOUBLE, "test_table"));
        Schema schema = new Schema(columnList);
        List<Cell> cells = new ArrayList<>();
        cells.add(new Cell(new TypedValue(INT, 1)));
        cells.add(new Cell(new TypedValue(STRING, "afk")));
        cells.add(new Cell(new TypedValue(DOUBLE, 233.3)));
        Row row = new Row(cells);
        evaluator = new RowEvaluator(new InheritedContext(), schema, row);
    }

    @Test
    public void allTest() throws SQLParseException {

        assertTypedValue(new TypedValue(INT, 2333), "2333");
        assertTypedValue(new TypedValue(DOUBLE, 666.0), "666.0");
        assertTypedValue(new TypedValue(STRING, "哈哈哈"), "'哈哈哈'");

        // 读取 row 信息
        assertTypedValue(new TypedValue(INT, 1), "id");
        assertTypedValue(new TypedValue(STRING, "afk"), "name");
        assertTypedValue(new TypedValue(DOUBLE, 233.3), "grade");

        // 算术
        assertTypedValue(new TypedValue(INT, 24), "1 + (2 + 3) * 4 + 6 / 2");
        assertTypedValue(new TypedValue(ERROR, "division by 0"), "1 / 0.00000");

        // 比较
        assertTypedValue(new TypedValue(INT, 1), "1 < 2");
        assertTypedValue(new TypedValue(INT, 0), "1 > 2");
        assertTypedValue(new TypedValue(INT, 0), "1 = 2");
        assertTypedValue(new TypedValue(INT, 1), "1 <= 2");
        assertTypedValue(new TypedValue(INT, 0), "1 >= 2");
        assertTypedValue(new TypedValue(INT, 1), "1 != 2");

        // 逻辑
        assertTypedValue(new TypedValue(INT, 0), "0 and 0");
        assertTypedValue(new TypedValue(INT, 0), "0 and 1");
        assertTypedValue(new TypedValue(INT, 0), "1 and 0");
        assertTypedValue(new TypedValue(INT, 1), "1 and 1");
        assertTypedValue(new TypedValue(INT, 0), "0 or 0");
        assertTypedValue(new TypedValue(INT, 1), "0 or 1");
        assertTypedValue(new TypedValue(INT, 1), "1 or 0");
        assertTypedValue(new TypedValue(INT, 1), "1 or 1");
        assertTypedValue(new TypedValue(INT, 0), "not 1");
        assertTypedValue(new TypedValue(INT, 1), "not 0");

        assertTypedValue(new TypedValue(INT, 0), "1 is null");
        assertTypedValue(new TypedValue(INT, 0), "2.333 is null");
        assertTypedValue(new TypedValue(INT, 0), "'null' is null");
        assertTypedValue(new TypedValue(INT, 1), "1 is not null");
        assertTypedValue(new TypedValue(INT, 1), "nuLL is null"); // 不管大小写
        assertTypedValue(new TypedValue(INT, 0), "nuLL is not null");


        assertTypedValue(new TypedValue(INT, 1), "1 between 1 and 2");
        assertTypedValue(new TypedValue(INT, 1), "1 between 0.99 and 1.01"); // 运行 int 和 double 比较
        assertTypedValue(new TypedValue(INT, 0), "1 between 2 and 3");
        assertTypedValue(new TypedValue(INT, 0), "1 between 2 and 1"); // 不能反过来

        assertTypedValue(new TypedValue(INT, 1), "'abc' like 'abc'");
        assertTypedValue(new TypedValue(INT, 0), "'abc' not like 'abc'");
        assertTypedValue(new TypedValue(INT, 1), "'abc' like '_bc'");
        assertTypedValue(new TypedValue(INT, 1), "'abc' like '__c'");
        assertTypedValue(new TypedValue(INT, 1), "'abc' like '___'");
        assertTypedValue(new TypedValue(INT, 0), "'abc' like '____'");
        assertTypedValue(new TypedValue(INT, 1), "'abc' like '%'");
        assertTypedValue(new TypedValue(INT, 1), "'abc' like '%b%'");
        assertTypedValue(new TypedValue(INT, 0), "'abc' like '%d%'");
        assertTypedValue(new TypedValue(INT, 1), "'中文' like '__'");
        assertTypedValue(new TypedValue(INT, 1), "'%' like '\\\\%'");
        assertTypedValue(new TypedValue(INT, 1), "'_' like '\\\\_'");
        assertTypedValue(new TypedValue(INT, 0), "'' like '_'");
        assertTypedValue(new TypedValue(INT, 1), "'' like '%'");

        assertTypedValue(new TypedValue(INT, 1), "1 in (1, 2, 3)");
        assertTypedValue(new TypedValue(INT, 1), "1 in (1.0)");
        assertTypedValue(new TypedValue(INT, 1), "1 in (id)"); // id = 1
        assertTypedValue(new TypedValue(INT, 0), "1 in (2333, 666)");

        // TODO：测试 subQuery
    }

    private void assertTypedValue(TypedValue typedValue, String expression) throws SQLParseException {
        Assert.assertEquals(typedValue, evaluator.evaluate(new Parser(new Lexer(new StringReader(expression))).expression()));
    }

}
