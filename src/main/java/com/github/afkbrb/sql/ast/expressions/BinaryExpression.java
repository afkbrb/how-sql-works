package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.ASTVisitor;

public class BinaryExpression implements Expression {

    private final BinaryOperatorType op;
    private final Expression left;
    private final Expression right;

    public BinaryExpression(BinaryOperatorType op, Expression left, Expression right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public BinaryOperatorType getOp() {
        return op;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
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
