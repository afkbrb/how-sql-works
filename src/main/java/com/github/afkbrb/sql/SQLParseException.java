package com.github.afkbrb.sql;

public class SQLParseException extends Exception {

    public SQLParseException() {
        super();
    }

    public SQLParseException(String format, Object... args) {
        super(String.format(format, args));
    }
}
