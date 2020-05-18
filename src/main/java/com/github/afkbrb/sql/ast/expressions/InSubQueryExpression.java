package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.ast.statements.SelectStatement;
import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class InSubQueryExpression implements Expression {

    private final boolean not;
    private final Expression target;
    private final SelectStatement subQuery;

    public InSubQueryExpression(boolean not, @NotNull Expression target, @NotNull SelectStatement subQuery) {
        this.not = not;
        this.target = Objects.requireNonNull(target);
        this.subQuery = Objects.requireNonNull(subQuery);
    }

    public boolean isNot() {
        return not;
    }

    @NotNull
    public Expression getTarget() {
        return target;
    }

    @NotNull
    public SelectStatement getSubQuery() {
        return subQuery;
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
