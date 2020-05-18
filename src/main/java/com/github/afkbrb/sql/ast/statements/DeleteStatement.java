package com.github.afkbrb.sql.ast.statements;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import com.github.afkbrb.sql.ast.expressions.Expression;
import org.jetbrains.annotations.Nullable;

/**
 * DELETE FROM tableName WHERE whereCondition;
 */
public class DeleteStatement extends Statement {

    private final String tableName;
    private final Expression whereCondition;

    public DeleteStatement(String tableName, @Nullable Expression whereCondition) {
        this.tableName = tableName;
        this.whereCondition = whereCondition;
    }

    public String getTableName() {
        return tableName;
    }

    @Nullable
    public Expression getWhereCondition() {
        return whereCondition;
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
