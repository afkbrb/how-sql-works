package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.ASTVisitor;

public class TextExpression implements Expression {

    private final String text;

    public TextExpression(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
