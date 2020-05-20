package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.ast.expressions.FunctionCallExpression;
import com.github.afkbrb.sql.ast.expressions.WildcardExpression;
import com.github.afkbrb.sql.model.*;
import com.github.afkbrb.sql.functions.Function;
import com.github.afkbrb.sql.functions.FunctionRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.github.afkbrb.sql.model.DataType.INT;
import static com.github.afkbrb.sql.utils.DataTypeUtils.isError;

public class RowsEvaluator extends AbstractEvaluator {

    private final List<Row> rows;

    public RowsEvaluator(@Nullable InheritedContext context, @NotNull Schema schema, @NotNull List<Row> rows) {
        // 在一个组中，作为分组依据的列对应的值在不同记录都是相同的，我们取第一条记录即可
        super(context, schema, rows.size() == 0 ? Row.EMPTY_ROW : rows.get(0));
        this.rows = rows;
    }

    @Override
    public TypedValue visit(FunctionCallExpression node) {
        Function function = FunctionRegistry.getFunction(node.getFunctionName());
        if (function == null) return new EvaluateError("Function %s not found", node.getFunctionName());

        if (function.isAggregate()) {
            if (node.getArgumentList().size() != 1) return new EvaluateError("Aggregate function expects one argument");
            Expression expression = node.getArgumentList().get(0);
            if (node.getFunctionName().equalsIgnoreCase("count") && expression instanceof WildcardExpression &&
                    ((WildcardExpression) expression).getTableName() == null) {
                // 对 count(*) 特殊处理
                return new TypedValue(INT, rows.size());
            }
            List<TypedValue> aggregatedArguments = new ArrayList<>();
            // 同一个表达式在不同 row 上进行求值，作为聚集函数的参数
            for (Row row : rows) {
                TypedValue typedValue = new RowEvaluator(context, schema, row).evaluate(expression);
                if (isError(typedValue)) return typedValue;
                aggregatedArguments.add(typedValue);
            }
            return function.call(aggregatedArguments);
        } else {
            List<TypedValue> arguments = new ArrayList<>();
            for (Expression expression : node.getArgumentList()) {
                TypedValue typedValue = expression.accept(this);
                if (isError(typedValue)) return typedValue;
                arguments.add(typedValue);
            }
            return function.call(arguments);
        }
    }
}
