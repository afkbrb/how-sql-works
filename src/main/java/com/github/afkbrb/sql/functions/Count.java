package com.github.afkbrb.sql.functions;

import com.github.afkbrb.sql.model.DataType;
import com.github.afkbrb.sql.model.TypedValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.afkbrb.sql.model.DataType.INT;
import static com.github.afkbrb.sql.utils.DataTypeUtils.isNull;

public class Count implements Function {
    @Override
    public String getName() {
        return "count";
    }

    @Override
    public DataType getReturnType() {
        return INT;
    }

    @Override
    public boolean isAggregate() {
        return true;
    }

    @Override
    public TypedValue call(@NotNull List<TypedValue> arguments) {
        int count = 0;
        for (TypedValue typedValue : arguments) {
            if (isNull(typedValue)) continue;
            count++;
        }
        return new TypedValue(INT, count);
    }
}
