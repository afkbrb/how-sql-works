package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.ASTVisitor;

import java.util.List;

public class InListExpression implements Expression {

    private final boolean not;
    private final Expression left;
    private final List<Expression> rightList;

    public InListExpression(boolean not, Expression left, List<Expression> rightList) {
        this.not = not;
        this.left = left;
        this.rightList = rightList;
    }

    public boolean isNot() {
        return not;
    }

    public Expression getLeft() {
        return left;
    }

    public List<Expression> getRightList() {
        return rightList;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
