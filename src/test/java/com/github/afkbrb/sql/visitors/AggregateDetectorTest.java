package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.Lexer;
import com.github.afkbrb.sql.Parser;
import com.github.afkbrb.sql.SQLParseException;
import com.github.afkbrb.sql.ast.expressions.Expression;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

public class AggregateDetectorTest {

    @Test
    public void testTrue() throws SQLParseException {
        String input = "1 + 1 + max(age) * 1";
        Parser parser = new Parser(new Lexer(new StringReader(input)));
        Expression expression = parser.expression();
        AggregateDetector detector = new AggregateDetector(expression);
        Assert.assertTrue(detector.detect());
    }

    @Test
    public void testFalse() throws SQLParseException {
        String input = "1 + 1 + upper(age) * 1";
        Parser parser = new Parser(new Lexer(new StringReader(input)));
        Expression expression = parser.expression();
        AggregateDetector detector = new AggregateDetector(expression);
        Assert.assertFalse(detector.detect());
    }
}
