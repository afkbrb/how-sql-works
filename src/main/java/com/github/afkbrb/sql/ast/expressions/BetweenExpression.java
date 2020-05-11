package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.ASTVisitor;

public class BetweenExpression implements Expression {

    private final Expression target;
    private final Expression left;
    private final Expression right;

    public BetweenExpression(Expression target, Expression left, Expression right) {
        this.target = target;
        this.left = left;
        this.right = right;
    }

    public Expression getTarget() {
        return target;
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
}
