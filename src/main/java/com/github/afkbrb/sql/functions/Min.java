package com.github.afkbrb.sql.functions;

import com.github.afkbrb.sql.model.DataType;
import com.github.afkbrb.sql.model.TypedValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.afkbrb.sql.model.DataType.DOUBLE;
import static com.github.afkbrb.sql.utils.DataTypeUtils.isNumber;

public class Min implements Function {

    @Override
    public String getName() {
        return "min";
    }

    @Override
    public DataType getReturnType() {
        return DataType.DOUBLE;
    }

    @Override
    public boolean isAggregate() {
        return true;
    }

    @Override
    public TypedValue call(@NotNull List<TypedValue> arguments) {
        Double min = null;
        for (TypedValue typedValue : arguments) {
            if (isNumber(typedValue)) {
                double doubleValue = ((Number) typedValue.getValue()).doubleValue();
                if (min == null) {
                    min = doubleValue;
                } else if (doubleValue < min) {
                    min = doubleValue;
                }
            }
        }
        return min == null ? TypedValue.NULL : new TypedValue(DOUBLE, min);
    }
}
