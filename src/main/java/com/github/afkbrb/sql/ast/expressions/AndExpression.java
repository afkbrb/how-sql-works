package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public class AndExpression implements Expression {

    private final Expression left;
    private final Expression right;

    public AndExpression(@NotNull Expression left, @NotNull Expression right) {
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
    }

    @NotNull
    public Expression getLeft() {
        return left;
    }

    @NotNull
    public Expression getRight() {
        return right;
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
