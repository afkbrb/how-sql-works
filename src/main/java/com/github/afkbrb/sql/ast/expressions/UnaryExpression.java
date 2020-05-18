package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UnaryExpression implements Expression {

    private final UnaryOperationType op;
    private final Expression expression;

    public UnaryExpression(UnaryOperationType op, @NotNull Expression expression) {
        this.op = op;
        this.expression = Objects.requireNonNull(expression);
    }

    public UnaryOperationType getOp() {
        return op;
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

    public enum UnaryOperationType {
        ADD,
        MINUS;

        @Override
        public String toString() {
            switch (this) {
                case ADD:
                    return "+";
                case MINUS:
                    return "-";
                default:
                    throw new IllegalArgumentException("unexpected unary operator type: " + this.name());
            }
        }
    }
}
