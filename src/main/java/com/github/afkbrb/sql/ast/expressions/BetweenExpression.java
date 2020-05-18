package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BetweenExpression implements Expression {

    private final Expression target;
    private final Expression left;
    private final Expression right;

    public BetweenExpression(@NotNull Expression target, @NotNull Expression left, @NotNull Expression right) {
        this.target = Objects.requireNonNull(target);
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
    }

    @NotNull
    public Expression getTarget() {
        return target;
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
