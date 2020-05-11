package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.ASTVisitor;

public class IsNullExpression implements Expression {

    private final boolean not;
    private final Expression expression;

    public IsNullExpression(boolean not, Expression expression) {
        this.not = not;
        this.expression = expression;
    }

    public boolean isNot() {
        return not;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
