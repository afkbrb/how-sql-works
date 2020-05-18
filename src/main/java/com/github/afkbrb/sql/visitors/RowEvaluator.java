package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.ast.expressions.FunctionCallExpression;
import com.github.afkbrb.sql.model.EvaluateError;
import com.github.afkbrb.sql.functions.Function;
import com.github.afkbrb.sql.functions.FunctionRegistry;
import com.github.afkbrb.sql.model.Row;
import com.github.afkbrb.sql.model.Schema;
import com.github.afkbrb.sql.model.TypedValue;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.github.afkbrb.sql.utils.DataTypeUtils.isError;


public class RowEvaluator extends AbstractEvaluator {

    public RowEvaluator(@Nullable Row row, @Nullable Schema schema) {
        super(row, schema);
    }

    @Override
    public TypedValue visit(FunctionCallExpression node) {
        Function function = FunctionRegistry.getFunction(node.getFunctionName());
        if (function == null) return new EvaluateError("function %s not found", node.getFunctionName());
        if (function.isAggregate()) return new EvaluateError("unexpected aggregate function call");

        List<TypedValue> arguments = new ArrayList<>();
        for (Expression expression : node.getArgumentList()) {
            TypedValue typedValue = expression.accept(this);
            if (isError(typedValue)) return typedValue;
            arguments.add(typedValue);
        }
        return function.call(arguments);
    }

}
