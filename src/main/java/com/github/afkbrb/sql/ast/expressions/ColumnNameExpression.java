package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ColumnNameExpression implements Expression {

    private final String tableName;
    private final String columnName;

    public ColumnNameExpression(@NotNull String columnName) {
        this(null, columnName);
    }

    public ColumnNameExpression(@Nullable String tableName, @NotNull String columnName) {
        this.tableName = tableName;
        this.columnName = Objects.requireNonNull(columnName);
    }

    @Nullable
    public String getTableName() {
        return tableName;
    }

    @NotNull
    public String getColumnName() {
        return columnName;
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
