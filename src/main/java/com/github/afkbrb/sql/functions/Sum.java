package com.github.afkbrb.sql.functions;

import com.github.afkbrb.sql.model.DataType;
import com.github.afkbrb.sql.model.TypedValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.afkbrb.sql.utils.DataTypeUtils.isNumber;

public class Sum implements Function {

    @Override
    public String getName() {
        return "sum";
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
        double sum = 0.0;
        boolean isNull = true;
        for (TypedValue typedValue : arguments) {
            if (isNumber(typedValue)) {
                double doubleValue = ((Number) typedValue.getValue()).doubleValue();
                sum += doubleValue;
                isNull = false;
            }
        }

        return isNull ? TypedValue.NULL : new TypedValue(DataType.DOUBLE, sum);
    }
}
