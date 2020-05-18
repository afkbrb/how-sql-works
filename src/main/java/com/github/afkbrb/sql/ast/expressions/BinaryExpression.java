package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BinaryExpression implements Expression {

    private final BinaryOperatorType op;
    private final Expression left;
    private final Expression right;

    public BinaryExpression(@NotNull BinaryOperatorType op, @NotNull Expression left, @NotNull Expression right) {
        this.op = Objects.requireNonNull(op);
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
    }

    @NotNull
    public BinaryOperatorType getOp() {
        return op;
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

    public enum BinaryOperatorType {
        ADD,
        MINUS,
        MULT,
        DIV,
        EQ,
        LT,
        GT,
        LE,
        GE,
        NE;

        @Override
        public String toString() {
            switch (this) {
                case ADD:
                    return "+";
                case MINUS:
                    return "-";
                case MULT:
                    return "*";
                case DIV:
                    return "/";
                case EQ:
                    return "=";
                case LT:
                    return "<";
                case GT:
                    return ">";
                case LE:
                    return "<=";
                case GE:
                    return ">=";
                case NE:
                    return "!=";
                default:
                    throw new IllegalArgumentException("unexpected binary operator type: " + this.name());
            }
        }
    }
}
