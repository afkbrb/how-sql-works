package com.github.afkbrb.sql.ast.statements;

import com.github.afkbrb.sql.ASTVisitor;
import com.github.afkbrb.sql.ast.ASTNode;
import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.utils.Pair;

import java.util.List;

/**
 * SELECT selectOption selectItemList FROM tableReferenceList WHERE whereCondition GROUP BY groupByExpression \
 * HAVING havingCondition ORDER BY orderExpression ASC/DESC LIMIT limitExpression OFFSET offsetExpression;
 */
public class SelectStatement extends Statement implements Expression {

    private final SelectOption selectOption;
    private final List<Pair<Expression, String>> selectItemList;
    private final TableReference tableReference;
    private final Expression whereCondition;
    private final Expression groupByExpression;
    private final Expression havingCondition;
    private final OrderBy orderBy;
    private final Limit limit;

    public SelectStatement(SelectOption selectOption, List<Pair<Expression, String>> selectItemList,
                           TableReference tableReference, Expression whereCondition,
                           Expression groupByExpression, Expression havingCondition, OrderBy orderBy, Limit limit) {
        this.selectOption = selectOption;
        this.selectItemList = ensureNonNull(selectItemList);
        this.tableReference = tableReference;
        this.whereCondition = whereCondition;
        this.groupByExpression = groupByExpression;
        this.havingCondition = havingCondition;
        this.orderBy = orderBy;
        this.limit = limit;
    }

    public SelectOption getSelectOption() {
        return selectOption;
    }

    public List<Pair<Expression, String>> getSelectItemList() {
        return selectItemList;
    }

    public TableReference getTableReference() {
        return tableReference;
    }

    public Expression getWhereCondition() {
        return whereCondition;
    }

    public Expression getGroupByExpression() {
        return groupByExpression;
    }

    public Expression getHavingCondition() {
        return havingCondition;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public Limit getLimit() {
        return limit;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public enum SelectOption {
        ALL,
        DISTINCT
    }

    public interface TableReference extends ASTNode {

    }

    public static class OrderBy {
        private final Expression orderByExpression;
        private final boolean desc;

        public OrderBy(Expression orderByExpression, boolean desc) {
            this.orderByExpression = orderByExpression;
            this.desc = desc;
        }

        public Expression getOrderByExpression() {
            return orderByExpression;
        }

        public boolean isDesc() {
            return desc;
        }
    }

    public static class Limit {

        private final Expression limitExpression;
        private final Expression offsetExpression;

        public Limit(Expression limitExpression, Expression offsetExpression) {
            this.limitExpression = limitExpression;
            this.offsetExpression = offsetExpression;
        }

        public Expression getLimitExpression() {
            return limitExpression;
        }

        public Expression getOffsetExpression() {
            return offsetExpression;
        }
    }

    public static class TableReferenceFactor implements TableReference {

        private final TableReference tableReference;
        private final String alias;

        public TableReferenceFactor(TableReference tableReference, String alias) {
            this.tableReference = tableReference;
            this.alias = alias;
        }

        public TableReference getTableReference() {
            return tableReference;
        }

        public String getAlias() {
            return alias;
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static class TableJoin implements TableReference {

        private final JoinType joinType;
        private final TableReference left;
        private final TableReference right;
        private final Expression on;

        public TableJoin(JoinType joinType, TableReference left, TableReference right, Expression on) {
            this.joinType = joinType;
            this.left = left;
            this.right = right;
            this.on = on;
        }

        public TableReference getLeft() {
            return left;
        }

        public TableReference getRight() {
            return right;
        }

        public JoinType getJoinType() {
            return joinType;
        }

        public Expression getOn() {
            return on;
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }

        public enum JoinType {
            INNER,
            LEFT
        }
    }

    public static class SubQueryFactor implements TableReference {

        private final SelectStatement selectStatement;
        private final String alias;

        public SubQueryFactor(SelectStatement selectStatement, String alias) {
            this.selectStatement = selectStatement;
            this.alias = alias;
        }

        public SelectStatement getSelectStatement() {
            return selectStatement;
        }

        public String getAlias() {
            return alias;
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }

    public static class RealTableFactor implements TableReference {

        private final String tableName;
        private final String tableNameAlias;

        public RealTableFactor(String tableName, String tableNameAlias) {
            this.tableName = tableName;
            this.tableNameAlias = tableNameAlias;
        }

        public String getTableName() {
            return tableName;
        }

        public String getTableNameAlias() {
            return tableNameAlias;
        }

        @Override
        public void accept(ASTVisitor visitor) {
            visitor.visit(this);
        }
    }
}
