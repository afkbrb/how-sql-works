package com.github.afkbrb.sql.ast.statements;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * UPDATE tableName SET updateList WHERE whereCondition;
 */
public class UpdateStatement extends Statement {

    private final String tableName;
    private final List<Pair<String, Expression>> updateList;
    private final Expression whereCondition;

    public UpdateStatement(String tableName, @NotNull List<Pair<String, Expression>> updateList, @Nullable Expression whereCondition) {
        this.tableName = tableName;
        this.updateList = Objects.requireNonNull(updateList);
        this.whereCondition = whereCondition;
    }

    public String getTableName() {
        return tableName;
    }

    @Nullable
    public Expression getWhereCondition() {
        return whereCondition;
    }

    @NotNull
    public List<Pair<String, Expression>> getUpdateList() {
        return updateList;
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
