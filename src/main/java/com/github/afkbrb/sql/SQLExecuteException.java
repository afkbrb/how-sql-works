package com.github.afkbrb.sql;

public class SQLExecuteException extends Exception {

    public SQLExecuteException() {
        super();
    }

    public SQLExecuteException(String format, Object... args) {
        super(String.format(format, args));
    }
}
