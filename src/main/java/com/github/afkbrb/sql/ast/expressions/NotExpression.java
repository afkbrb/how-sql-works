package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NotExpression implements Expression {

    private final Expression expression;

    public NotExpression(@NotNull Expression expression) {
        this.expression = Objects.requireNonNull(expression);
    }

    @NotNull
    public Expression getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(Visitor<? extends T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return new ToStringVisitor(this).toString();
    }
}
