package com.github.afkbrb.sql.ast.statements;

import com.github.afkbrb.sql.ASTVisitor;

/**
 * DROP TABLE tableName;
 */
public class DropStatement extends Statement {

    private final String tableName;

    public DropStatement(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
