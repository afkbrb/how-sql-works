package com.github.afkbrb.sql.ast.statements;

import com.github.afkbrb.sql.ASTVisitor;

import java.util.List;

/**
 * CREATE TABLE tableName (columnDefinitionList);
 */
public class CreateStatement extends Statement {

    private final String tableName;
    private final List<ColumnDefinition> columnDefinitionList;

    public CreateStatement(String tableName, List<ColumnDefinition> columnDefinitionList) {
        this.tableName = tableName;
        this.columnDefinitionList = ensureNonNull(columnDefinitionList);
    }

    public List<ColumnDefinition> getColumnDefinitionList() {
        return columnDefinitionList;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public enum DataType {
        INT,
        DOUBLE,
        TEXT
    }

    public static class ColumnDefinition {

        private final String columnName;
        private final DataType columnType;

        public ColumnDefinition(String columnName, DataType columnType) {
            this.columnName = columnName;
            this.columnType = columnType;
        }

        public DataType getColumnType() {
            return columnType;
        }

        public String getColumnName() {
            return columnName;
        }
    }
}
