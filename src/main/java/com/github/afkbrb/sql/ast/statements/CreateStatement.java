package com.github.afkbrb.sql.ast.statements;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import com.github.afkbrb.sql.model.DataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * CREATE TABLE tableName (columnDefinitionList);
 */
public class CreateStatement extends Statement {

    private final String tableName;
    private final List<ColumnDefinition> columnDefinitionList;

    public CreateStatement(String tableName, @NotNull List<ColumnDefinition> columnDefinitionList) {
        this.tableName = tableName;
        this.columnDefinitionList = Objects.requireNonNull(columnDefinitionList);
    }

    @NotNull
    public List<ColumnDefinition> getColumnDefinitionList() {
        return columnDefinitionList;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public <T> T accept(Visitor<? extends T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return new ToStringVisitor(this).toString();
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
