package com.github.afkbrb.sql.utils;

import com.github.afkbrb.sql.model.TypedValue;

import static com.github.afkbrb.sql.model.DataType.*;

public final class DataTypeUtils {

    public static boolean isInt(TypedValue typedValue) {
        return typedValue.getDataType() == INT;
    }

    public static boolean isDouble(TypedValue typedValue) {
        return typedValue.getDataType() == DOUBLE;
    }

    public static boolean isNumber(TypedValue typedValue) {
        return typedValue.getDataType() == INT || typedValue.getDataType() == DOUBLE;
    }

    public static boolean isString(TypedValue typedValue) {
        return typedValue.getDataType() == STRING;
    }

    public static boolean isError(TypedValue typedValue) {
        return typedValue.getDataType() == ERROR;
    }

    public static boolean isNull(TypedValue typedValue) {
        return typedValue.getDataType() == NULL;
    }

    private DataTypeUtils() {
    }

}
