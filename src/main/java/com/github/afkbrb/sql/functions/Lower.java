package com.github.afkbrb.sql.functions;

import com.github.afkbrb.sql.model.DataType;
import com.github.afkbrb.sql.model.EvaluateError;
import com.github.afkbrb.sql.model.TypedValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.github.afkbrb.sql.utils.DataTypeUtils.isString;

public class Lower implements Function {

    @Override
    public String getName() {
        return "lower";
    }

    @Override
    public DataType getReturnType() {
        return DataType.STRING;
    }

    @Override
    public boolean isAggregate() {
        return false;
    }

    @Override
    public TypedValue call(@NotNull List<TypedValue> arguments) {
        if (arguments.size() == 1 && isString(arguments.get(0))) {
            return new TypedValue(DataType.STRING, ((String) arguments.get(0).getValue()).toLowerCase());
        }

        return new EvaluateError("%s expects a string", getName());
    }
}
