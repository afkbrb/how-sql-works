package com.github.afkbrb.sql.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.github.afkbrb.sql.model.DataType.*;
import static com.github.afkbrb.sql.utils.DataTypeUtils.*;
import static com.github.afkbrb.sql.utils.JsonUtils.jsonEscape;

public class TypedValue implements Comparable<TypedValue> {

    public static final TypedValue NULL = new TypedValue(DataType.NULL, "null");

    private final DataType dataType;
    private Object value;

    public TypedValue(@NotNull DataType dataType, @NotNull Object value) {
        Objects.requireNonNull(dataType);
        Objects.requireNonNull(value);
        if (dataType == INT && value instanceof Number) {
            this.dataType = dataType;
            this.value = ((Number) value).intValue();
        } else if (dataType == DOUBLE && value instanceof Number) {
            this.dataType = dataType;
            this.value = ((Number) value).doubleValue();
        } else if ((dataType == STRING || dataType == DataType.NULL || dataType == ERROR) && value instanceof String) {
            this.dataType = dataType;
            this.value = value;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @NotNull
    public DataType getDataType() {
        return dataType;
    }

    @NotNull
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\": ").append("\"").append(dataType).append("\"").append(", ").append("\"value\": ");
        switch (dataType) {
            case INT:
                sb.append(((Number) value).intValue());
                break;
            case DOUBLE:
                sb.append(((Number) value).doubleValue());
                break;
            case STRING:
            case ERROR:
                sb.append("\"").append(jsonEscape((String) value)).append("\"");
                break;
            case NULL:
                sb.append("null");
                break;
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof TypedValue)) return false;
        TypedValue otherValue = (TypedValue) other;
        if (dataType == otherValue.dataType) {
            return value.equals(otherValue.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataType, value);
    }


    @Override
    public int compareTo(@NotNull TypedValue other) {
        if (equals(other)) return 0;
        if (isNull(this)) return -1; // NULL 总是最小的
        if (isNull(other)) return 1;
        if (isString(this) && isString(other)) {
            return ((String) value).compareTo((String) other.value);
        } else if (isNumber(this) && isNumber(other)) {
            Double thisValue = ((Number) this.value).doubleValue();
            Double otherValue = ((Number) other.value).doubleValue();
            return thisValue.compareTo(otherValue);
        }
        throw new IllegalArgumentException();
    }
}
