package com.github.afkbrb.sql.model;

import com.github.afkbrb.sql.model.TypedValue;

import static com.github.afkbrb.sql.model.DataType.ERROR;

public class EvaluateError extends TypedValue {

    public EvaluateError(String format, Object... args) {
        super(ERROR, String.format(format, args));
    }
}
