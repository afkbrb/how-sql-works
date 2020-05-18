package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class InListExpression implements Expression {

    private final boolean not;
    private final Expression target;
    private final List<Expression> list;

    public InListExpression(boolean not, @NotNull Expression target, @NotNull List<Expression> list) {
        this.not = not;
        this.target = Objects.requireNonNull(target);
        this.list = Objects.requireNonNull(list);
    }

    public boolean isNot() {
        return not;
    }

    @NotNull
    public Expression getTarget() {
        return target;
    }

    @NotNull
    public List<Expression> getList() {
        return list;
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
