package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.ASTVisitor;

public class LikeExpression implements Expression {

    private final boolean not;
    private final Expression left;
    private final Expression right;

    public LikeExpression(boolean not, Expression left, Expression right) {
        this.not = not;
        this.left = left;
        this.right = right;
    }

    public boolean isNot() {
        return not;
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
