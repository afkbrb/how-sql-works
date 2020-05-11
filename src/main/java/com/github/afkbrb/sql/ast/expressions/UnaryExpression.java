package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.ASTVisitor;

public class UnaryExpression implements Expression {

    private final UnaryOperationType op;
    private final Expression expression;

    public UnaryExpression(UnaryOperationType op, Expression expression) {
        this.op = op;
        this.expression = expression;
    }

    public UnaryOperationType getOp() {
        return op;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
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
