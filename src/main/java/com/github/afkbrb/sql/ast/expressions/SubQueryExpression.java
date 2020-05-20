package com.github.afkbrb.sql.ast.expressions;

import com.github.afkbrb.sql.ast.statements.SelectStatement;
import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 由于不支持 ANY/ALL（SQLite 也不支持，但可以通过使用 MAX/MIN 实现相同功能，所以不提供也行），
 * IN 又在 {@link InSubQueryExpression} 中实现了，此处可以要求子查询结果只有一行一列。
 * 这样的话对子查询进行求值就可以直接返回一个 TypedValue，而不是一个列表。
 * EXISTS 也在这边实现了。
 */
public class SubQueryExpression implements Expression {

    private final boolean exists;
    private final SelectStatement subQuery;

    public SubQueryExpression(boolean exists, @NotNull SelectStatement subQuery) {
        this.exists = exists;
        this.subQuery = Objects.requireNonNull(subQuery);
    }

    @NotNull
    public SelectStatement getSubQuery() {
        return subQuery;
    }

    public boolean isExists() {
        return exists;
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
