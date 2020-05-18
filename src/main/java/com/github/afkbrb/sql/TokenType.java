package com.github.afkbrb.sql;

import java.util.HashMap;
import java.util.Map;

public enum TokenType {

    EOF,

    IDENTIFIER,

    INT_LITERAL,
    DOUBLE_LITERAL,
    STRING_LITERAL,

    EQ,
    LT,
    GT,
    LE,
    GE,
    NE,

    ADD,
    MINUS,
    MULT,
    DIV,

    COMMA, // ','
    DOT, // '.'
    SEMICOLON, // ';'
    OPEN_PAR, // '('
    CLOSE_PAR, // ')'

    // 关键字
    ALL,
    AND,
    AS,
    ASC,
    BETWEEN,
    BY,
    CREATE,
    DELETE,
    DESC,
    DISTINCT,
    DOUBLE,
    DROP,
    FALSE,
    FROM,
    GROUP,
    HAVING,
    IN,
    INNER,
    INSERT,
    INT,
    INTO,
    IS,
    JOIN,
    LEFT,
    LIKE,
    LIMIT,
    NOT,
    NULL,
    OFFSET,
    ON,
    OR,
    ORDER,
    OUTER,
    RIGHT,
    SELECT,
    SET,
    STRING,
    TABLE,
    TRUE,
    UPDATE,
    VALUES,
    WHERE;

    public static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("ALL", ALL);
        keywords.put("AND", AND);
        keywords.put("AS", AS);
        keywords.put("ASC", ASC);
        keywords.put("BETWEEN", BETWEEN);
        keywords.put("BY", BY);
        keywords.put("CREATE", CREATE);
        keywords.put("DELETE", DELETE);
        keywords.put("DESC", DESC);
        keywords.put("DISTINCT", DISTINCT);
        keywords.put("DOUBLE", DOUBLE);
        keywords.put("DROP", DROP);
        keywords.put("FALSE", FALSE);
        keywords.put("FROM", FROM);
        keywords.put("GROUP", GROUP);
        keywords.put("HAVING", HAVING);
        keywords.put("IN", IN);
        keywords.put("INNER", INNER);
        keywords.put("INSERT", INSERT);
        keywords.put("INT", INT);
        keywords.put("INTO", INTO);
        keywords.put("IS", IS);
        keywords.put("JOIN", JOIN);
        keywords.put("LEFT", LEFT);
        keywords.put("LIKE", LIKE);
        keywords.put("LIMIT", LIMIT);
        keywords.put("NOT", NOT);
        keywords.put("NULL", NULL);
        keywords.put("OFFSET", OFFSET);
        keywords.put("ON", ON);
        keywords.put("OR", OR);
        keywords.put("ORDER", ORDER);
        keywords.put("OUTER", OUTER);
        keywords.put("RIGHT", RIGHT);
        keywords.put("SELECT", SELECT);
        keywords.put("SET", SET);
        keywords.put("STRING", STRING);
        keywords.put("TABLE", TABLE);
        keywords.put("TRUE", TRUE);
        keywords.put("UPDATE", UPDATE);
        keywords.put("VALUES", VALUES);
        keywords.put("WHERE", WHERE);
    }

}
