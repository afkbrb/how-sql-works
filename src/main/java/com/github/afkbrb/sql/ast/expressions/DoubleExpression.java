package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.ASTVisitor;

public class DoubleExpression implements Expression {

    private final double value;

    public DoubleExpression(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
