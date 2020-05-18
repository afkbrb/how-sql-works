package com.github.afkbrb.sql.ast.statements;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import com.github.afkbrb.sql.ast.expressions.Expression;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * INSERT INTO tableName (columnList) VALUES (valueList);
 */
public class InsertStatement extends Statement {

    private final String tableName;
    private final List<String> columnList;
    private final List<Expression> valueList;

    public InsertStatement(String tableName, @NotNull List<String> columnList, @NotNull List<Expression> valueList) {
        this.tableName = tableName;
        this.columnList = Objects.requireNonNull(columnList);
        this.valueList = Objects.requireNonNull(valueList);
    }

    public String getTableName() {
        return tableName;
    }

    @NotNull
    public List<Expression> getValueList() {
        return valueList;
    }

    @NotNull
    public List<String> getColumnList() {
        return columnList;
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
