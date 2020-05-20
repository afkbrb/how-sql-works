package com.github.afkbrb.sql.ast.statements;

import com.github.afkbrb.sql.visitors.ToStringVisitor;
import com.github.afkbrb.sql.visitors.Visitor;
import com.github.afkbrb.sql.ast.Node;
import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * SELECT selectOption selectItemList FROM tableReferenceList WHERE whereCondition groupBy orderBy limit;
 */
public class SelectStatement extends Statement implements Expression {

    private final List<Pair<Expression, String>> selectItemList;
    private final TableReference tableReference;
    private final Expression whereCondition;
    private final GroupBy groupBy;
    private final OrderBy orderBy;
    private final Limit limit;

    public SelectStatement(@NotNull List<Pair<Expression, String>> selectItemList,
                           @Nullable TableReference tableReference, @Nullable Expression whereCondition,
                           @Nullable GroupBy groupBy, @Nullable OrderBy orderBy, @Nullable Limit limit) {
        this.selectItemList = Objects.requireNonNull(selectItemList);
        this.tableReference = tableReference;
        this.whereCondition = whereCondition;
        this.groupBy = groupBy;
        this.orderBy = orderBy;
        this.limit = limit;
    }

    @NotNull
    public List<Pair<Expression, String>> getSelectItemList() {
        return selectItemList;
    }

    @Nullable
    public TableReference getTableReference() {
        return tableReference;
    }

    @Nullable
    public Expression getWhereCondition() {
        return whereCondition;
    }

    @Nullable
    public GroupBy getGroupBy() {
        return groupBy;
    }

    @Nullable
    public OrderBy getOrderBy() {
        return orderBy;
    }

    @Nullable
    public Limit getLimit() {
        return limit;
    }

    @Override
    public <T> T accept(Visitor<? extends T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return new ToStringVisitor(this).toString();
    }

    public enum SelectOption {
        ALL,
        DISTINCT
    }

    public interface TableReference extends Node {

    }

    public static class GroupBy implements Node {

        private final List<Expression> groupByList;
        private final Expression havingCondition;

        public GroupBy(@NotNull List<Expression> expressionList, @Nullable Expression havingCondition) {
            this.groupByList = Objects.requireNonNull(expressionList);
            this.havingCondition = havingCondition;
        }

        @NotNull
        public List<Expression> getGroupByList() {
            return groupByList;
        }

        @Nullable
        public Expression getHavingCondition() {
            return havingCondition;
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

    public static class OrderBy implements Node {

        private final List<Pair<Expression, Boolean>> orderByList;

        public OrderBy(@NotNull List<Pair<Expression, Boolean>> orderByList) {
            this.orderByList = Objects.requireNonNull(orderByList);
        }

        @NotNull
        public List<Pair<Expression, Boolean>> getOrderByList() {
            return orderByList;
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

    public static class Limit implements Node {

        private final Expression limitExpression;
        private final Expression offsetExpression;

        public Limit(@NotNull Expression limitExpression, @Nullable Expression offsetExpression) {
            this.limitExpression = Objects.requireNonNull(limitExpression);
            this.offsetExpression = offsetExpression;
        }

        @NotNull
        public Expression getLimitExpression() {
            return limitExpression;
        }

        @Nullable
        public Expression getOffsetExpression() {
            return offsetExpression;
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

    public static class TableJoin implements TableReference {

        private final JoinType joinType;
        private final TableReference left;
        private final TableReference right;
        private final Expression on;

        public TableJoin(JoinType joinType, @NotNull TableReference left, @NotNull TableReference right, @Nullable Expression on) {
            this.joinType = joinType;
            this.left = Objects.requireNonNull(left);
            this.right = Objects.requireNonNull(right);
            this.on = on;
        }

        @NotNull
        public TableReference getLeft() {
            return left;
        }

        @NotNull
        public TableReference getRight() {
            return right;
        }

        public JoinType getJoinType() {
            return joinType;
        }

        @Nullable
        public Expression getOn() {
            return on;
        }

        @Override
        public <T> T accept(Visitor<? extends T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String toString() {
            return new ToStringVisitor(this).toString();
        }

        public enum JoinType {
            INNER,
            LEFT
        }
    }

    public static class DerivedTable implements TableReference {

        private final SelectStatement selectStatement;
        private final String alias;

        public DerivedTable(@NotNull SelectStatement selectStatement, @NotNull String alias) {
            this.selectStatement = Objects.requireNonNull(selectStatement);
            this.alias = Objects.requireNonNull(alias);
        }

        @NotNull
        public SelectStatement getSelectStatement() {
            return selectStatement;
        }

        @NotNull
        public String getAlias() {
            return alias;
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
        public <T> T accept(Visitor<? extends T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public String toString() {
            return new ToStringVisitor(this).toString();
        }
    }
}
