package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.visitors.Visitor;
import org.jetbrains.annotations.Nullable;

public class WildcardExpression implements Expression {

    private final String tableName;

    public WildcardExpression() {
        this(null);
    }

    public WildcardExpression(@Nullable String tableName) {
        this.tableName = tableName;
    }

    @Nullable
    public String getTableName() {
        return tableName;
    }


    @Override
    public <T> T accept(Visitor<? extends T> visitor) {
        return visitor.visit(this);
    }
}
