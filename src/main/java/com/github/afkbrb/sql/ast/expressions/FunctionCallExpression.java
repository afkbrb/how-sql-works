package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class FunctionCallExpression implements Expression {

    private final String functionName;
    private final List<Expression> argumentList;

    public FunctionCallExpression(String functionName, @NotNull List<Expression> argumentList) {
        this.functionName = functionName;
        this.argumentList = Objects.requireNonNull(argumentList);
    }

    public String getFunctionName() {
        return functionName;
    }

    @NotNull
    public List<Expression> getArgumentList() {
        return argumentList;
    }

    @Override
    public <T> T accept(Visitor<? extends T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return new ToStringVisitor(this).toString();
    }
}
