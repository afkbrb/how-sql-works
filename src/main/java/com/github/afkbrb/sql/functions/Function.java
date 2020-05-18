package com.github.afkbrb.sql.functions;

import com.github.afkbrb.sql.model.DataType;
import com.github.afkbrb.sql.model.EvaluateError;
import com.github.afkbrb.sql.model.TypedValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Function {

    String getName();

    DataType getReturnType();

    boolean isAggregate();

    /**
     * 代码实现确保了 arguments 中不会包含 {@link EvaluateError}，但可能是 NULL 类型的 TypedData。
     */
    TypedValue call(@NotNull List<TypedValue> arguments);
}
