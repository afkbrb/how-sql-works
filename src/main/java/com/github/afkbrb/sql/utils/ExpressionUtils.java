package com.github.afkbrb.sql.utils;

import com.github.afkbrb.sql.ast.expressions.Expression;

public final class ExpressionUtils {

    public static String trimParenthesis(Expression expression) {
        if (expression == null) return "null";
        String toString = expression.toString();
        if (toString.startsWith("(") && toString.endsWith(")")) {
            toString = toString.substring(1, toString.length() - 1);
        }
        return toString;
    }

    private ExpressionUtils() {}
}
