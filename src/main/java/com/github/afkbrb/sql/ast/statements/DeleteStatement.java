package com.github.afkbrb.sql.ast.statements;

import com.github.afkbrb.sql.ASTVisitor;
import com.github.afkbrb.sql.ast.expressions.Expression;

/**
 * DELETE FROM tableName WHERE whereCondition;
 */
public class DeleteStatement extends Statement {

    private final String tableName;
    private final Expression whereCondition;

    public DeleteStatement(String tableName, Expression whereCondition) {
        this.tableName = tableName;
        this.whereCondition = whereCondition;
    }

    public String getTableName() {
        return tableName;
    }

    public Expression getWhereCondition() {
        return whereCondition;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
