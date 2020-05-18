package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class IsNullExpression implements Expression {

    private final boolean not;
    private final Expression expression;

    public IsNullExpression(boolean not, @NotNull Expression expression) {
        this.not = not;
        this.expression = Objects.requireNonNull(expression);
    }

    public boolean isNot() {
        return not;
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
