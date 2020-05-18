package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.ast.expressions.FunctionCallExpression;
import com.github.afkbrb.sql.functions.Function;
import com.github.afkbrb.sql.functions.FunctionRegistry;

public class AggregateDetector extends DefaultVisitor<Void> {

    private final Expression expression;
    private boolean isAggregate = false;

    public AggregateDetector(Expression expression) {
        this.expression = expression;
    }

    public boolean detect() {
        if (isAggregate) return true;
        expression.accept(this);
        return isAggregate;
    }

    @Override
    public Void visit(FunctionCallExpression node) {
        if (isAggregate) return null;
        String functionName = node.getFunctionName();
        Function function = FunctionRegistry.getFunction(functionName);
        if (function != null && function.isAggregate()) {
            isAggregate = true;
        }
        return null;
    }
}
