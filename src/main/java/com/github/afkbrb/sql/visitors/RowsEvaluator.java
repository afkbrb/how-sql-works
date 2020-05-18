package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.ast.expressions.FunctionCallExpression;
import com.github.afkbrb.sql.ast.expressions.IdentifierExpression;
import com.github.afkbrb.sql.model.EvaluateError;
import com.github.afkbrb.sql.functions.Function;
import com.github.afkbrb.sql.functions.FunctionRegistry;
import com.github.afkbrb.sql.model.Row;
import com.github.afkbrb.sql.model.Schema;
import com.github.afkbrb.sql.model.TypedValue;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.github.afkbrb.sql.model.DataType.INT;
import static com.github.afkbrb.sql.utils.DataTypeUtils.isError;

public class RowsEvaluator extends AbstractEvaluator {

    private final List<Row> rows;

    public RowsEvaluator(@Nullable List<Row> rows, @Nullable Schema schema) {
        // 在一个组中，作为分组依据的列对应的值在不同记录都是相同的，我们取第一条记录即可
        super(rows == null || rows.size() == 0 ? null : rows.get(0), schema);
        this.rows = rows;
    }

    @Override
    public TypedValue visit(FunctionCallExpression node) {
        if (rows == null) return new EvaluateError("rows is null");
        Function function = FunctionRegistry.getFunction(node.getFunctionName());
        if (function == null) return new EvaluateError("function %s not found", node.getFunctionName());

        if (function.isAggregate()) {
            if (node.getArgumentList().size() != 1) return new EvaluateError("aggregate function expects one argument");
            Expression expression = node.getArgumentList().get(0);
            if (node.getFunctionName().equalsIgnoreCase("count") && expression instanceof IdentifierExpression &&
                    ((IdentifierExpression) expression).getIdentifier().equals("*")) {
                // 对 count(*) 特殊处理
                return new TypedValue(INT, rows.size());
            }
            List<TypedValue> aggregatedArguments = new ArrayList<>();
            // 同一个表达式在不同 row 上进行求值，作为聚集函数的参数
            for (Row row : rows) {
                TypedValue typedValue = new RowEvaluator(row, schema).evaluate(expression);
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
