package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.ast.expressions.FunctionCallExpression;
import com.github.afkbrb.sql.functions.Function;
import com.github.afkbrb.sql.functions.FunctionRegistry;
import com.github.afkbrb.sql.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.github.afkbrb.sql.utils.DataTypeUtils.isError;


public class RowEvaluator extends AbstractEvaluator {

    public RowEvaluator(@Nullable InheritedContext context, @NotNull Schema schema, @NotNull Row row) {
        super(context, schema, row);
    }

    @Override
    public TypedValue visit(FunctionCallExpression node) {
        Function function = FunctionRegistry.getFunction(node.getFunctionName());
        if (function == null) return new EvaluateError("Function %s not found", node.getFunctionName());
        if (function.isAggregate())
            return new EvaluateError("invalid use of aggregate function %s", node.getFunctionName());

        List<TypedValue> arguments = new ArrayList<>();
        for (Expression expression : node.getArgumentList()) {
            TypedValue typedValue = expression.accept(this);
            if (isError(typedValue)) return typedValue;
            arguments.add(typedValue);
        }
        return function.call(arguments);
    }

}
