package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.ASTVisitor;

import java.util.List;

public class FunctionCallExpression implements Expression {

    private final String functionName;
    private final List<Expression> argumentList;

    public FunctionCallExpression(String functionName, List<Expression> argumentList) {
        this.functionName = functionName;
        this.argumentList = argumentList;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Expression> getArgumentList() {
        return argumentList;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
