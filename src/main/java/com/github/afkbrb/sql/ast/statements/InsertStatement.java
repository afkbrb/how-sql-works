package com.github.afkbrb.sql.ast.statements;

import com.github.afkbrb.sql.ASTVisitor;
import com.github.afkbrb.sql.ast.expressions.Expression;

import java.util.List;

/**
 * INSERT INTO tableName (columnList) VALUES (valueList);
 */
public class InsertStatement extends Statement {

    private final String tableName;
    private final List<String> columnList;
    private final List<Expression> valueList;

    public InsertStatement(String tableName, List<String> columnList, List<Expression> valueList) {
        this.tableName = tableName;
        this.columnList = ensureNonNull(columnList);
        this.valueList = ensureNonNull(valueList);
    }

    public String getTableName() {
        return tableName;
    }

    public List<Expression> getValueList() {
        return valueList;
    }

    public List<String> getColumnList() {
        return columnList;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
