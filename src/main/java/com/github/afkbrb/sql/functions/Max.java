package com.github.afkbrb.sql.functions;

import com.github.afkbrb.sql.model.DataType;
import com.github.afkbrb.sql.model.TypedValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.afkbrb.sql.model.DataType.DOUBLE;
import static com.github.afkbrb.sql.utils.DataTypeUtils.isNull;

public class Max implements Function {

    @Override
    public String getName() {
        return "max";
    }

    @Override
    public boolean isAggregate() {
        return true;
    }

    @Override
    public DataType getReturnType() {
        return DOUBLE;
    }

    @Override
    public TypedValue call(@NotNull List<TypedValue> arguments) {
        Double max = null;
        for (TypedValue typedValue : arguments) {
            if (isNull(typedValue)) continue;
            double doubleValue = ((Number) typedValue.getValue()).doubleValue();
            if (max == null) {
                max = doubleValue;
            } else if (doubleValue > max) {
                max = doubleValue;
            }
        }
        return max == null ? TypedValue.NULL : new TypedValue(DOUBLE, max);
    }
}
