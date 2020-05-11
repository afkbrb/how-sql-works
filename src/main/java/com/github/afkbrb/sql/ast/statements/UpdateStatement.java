package com.github.afkbrb.sql.ast.statements;

import com.github.afkbrb.sql.ASTVisitor;
import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.utils.Pair;

import java.util.List;

/**
 * UPDATE tableName SET updateList WHERE whereCondition;
 */
public class UpdateStatement extends Statement {

    private final String tableName;
    private final List<Pair<String, Expression>> updateList;
    private final Expression whereCondition;

    public UpdateStatement(String tableName, List<Pair<String, Expression>> updateList, Expression whereCondition) {
        this.tableName = tableName;
        this.updateList = ensureNonNull(updateList);
        this.whereCondition = whereCondition;
    }

    public String getTableName() {
        return tableName;
    }

    public Expression getWhereCondition() {
        return whereCondition;
    }

    public List<Pair<String, Expression>> getSetList() {
        return updateList;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
