package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.ast.Node;
import com.github.afkbrb.sql.ast.expressions.*;
import com.github.afkbrb.sql.ast.statements.*;
import com.github.afkbrb.sql.ast.statements.SelectStatement.*;
import com.github.afkbrb.sql.utils.Pair;

import java.util.List;

public class ToStringVisitor extends DefaultVisitor<Void> {

    private final StringBuilder sb = new StringBuilder();

    private final Node root;

    public ToStringVisitor(Node root) {
        this.root = root;
    }

    @Override
    public String toString() {
        if (sb.length() == 0) {
            root.accept(this);
            if (root instanceof Statement) sb.append(";");
        }
        return sb.toString();
    }

    @Override
    public Void visit(CreateStatement node) {
        sb.append("CREATE TABLE ").append(node.getTableName());

        sb.append("(");
        boolean first = true;
        for (CreateStatement.ColumnDefinition columnDefinition : node.getColumnDefinitionList()) {
            if (!first) sb.append(", ");
            sb.append(columnDefinition.getColumnName()).append(" ").append(columnDefinition.getColumnType());
            first = false;
        }
        sb.append(")");
        return null;
    }

    @Override
    public Void visit(DeleteStatement node) {
        sb.append("DELETE FROM ").append(node.getTableName());
        if (node.getWhereCondition() != null) {
            sb.append(" WHERE ");
            node.getWhereCondition().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(DropStatement node) {
        sb.append("DROP TABLE ").append(node.getTableName());
        return null;
    }

    @Override
    public Void visit(InsertStatement node) {
        sb.append("INSERT INTO ").append(node.getTableName()).append(" ");
        if (node.getColumnList().size() > 0) {
            sb.append("(");
            boolean first = true;
            for (String columnName : node.getColumnList()) {
                if (!first) sb.append(", ");
                sb.append(columnName);
                first = false;
            }
            sb.append(") ");
        }

        sb.append("VALUES ");

        sb.append("(");
        boolean first = true;
        for (Expression value : node.getValueList()) {
            if (!first) sb.append(", ");
            value.accept(this);
            first = false;
        }
        sb.append(")");
        return null;
    }

    private void appendExpressionList(List<Expression> expressionList) {
        boolean first = true;
        for (Expression expression : expressionList) {
            if (!first) sb.append(", ");
            expression.accept(this);
            first = false;
        }
    }

    @Override
    public Void visit(SelectStatement node) {
        sb.append("SELECT ");

        boolean first = true;
        for (Pair<Expression, String> pair : node.getSelectItemList()) {
            if (!first) sb.append(", ");
            pair.getKey().accept(this);
            if (pair.getValue() != null) {
                sb.append(" AS ").append(pair.getValue());
            }
            first = false;
        }

        if (node.getTableReference() != null) {
            sb.append(" FROM ");
            node.getTableReference().accept(this);
        }

        if (node.getWhereCondition() != null) {
            sb.append(" WHERE ");
            node.getWhereCondition().accept(this);
        }

        if (node.getGroupBy() != null) {
            node.getGroupBy().accept(this);
        }

        if (node.getOrderBy() != null) {
            node.getOrderBy().accept(this);
        }

        if (node.getLimit() != null) {
            sb.append(" LIMIT ");
            node.getLimit().getLimitExpression().accept(this);
            if (node.getLimit().getOffsetExpression() != null) {
                sb.append(" OFFSET ");
                node.getLimit().getOffsetExpression().accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visit(UpdateStatement node) {
        sb.append("UPDATE ").append(node.getTableName()).append(" SET ");
        boolean first = true;
        for (Pair<String, Expression> pair : node.getUpdateList()) {
            if (!first) sb.append(", ");
            sb.append(pair.getKey());
            sb.append(" = ");
            pair.getValue().accept(this);
            first = false;
        }

        if (node.getWhereCondition() != null) {
            sb.append(" WHERE ");
            node.getWhereCondition().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(TableJoin node) {
        sb.append("(");
        node.getLeft().accept(this);
        sb.append(" ").append(node.getJoinType()).append(" JOIN ");
        node.getRight().accept(this);
        if (node.getOn() != null) {
            sb.append(" ON ");
            node.getOn().accept(this);
        }
        sb.append(")");
        return null;
    }

    @Override
    public Void visit(DerivedTable node) {
        sb.append("(");

        sb.append("(");
        node.getSelectStatement().accept(this);
        sb.append(")");
        sb.append(" AS ").append(node.getAlias());

        sb.append(")");
        return null;
    }

    @Override
    public Void visit(RealTableFactor node) {
        sb.append(node.getTableName());
        if (node.getTableNameAlias() != null) {
            sb.append(" AS ").append(node.getTableNameAlias());
        }
        return null;
    }

    @Override
    public Void visit(GroupBy groupBy) {
        sb.append(" GROUP BY ");
        appendExpressionList(groupBy.getGroupByList());
        if (groupBy.getHavingCondition() != null) {
            sb.append(" HAVING ");
            groupBy.getHavingCondition().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(OrderBy orderBy) {
        sb.append(" ORDER BY ");
        List<Pair<Expression, Boolean>> orderByList = orderBy.getOrderByList();
        boolean first = true;
        for (Pair<Expression, Boolean> pair : orderByList) {
            if (!first) sb.append(", ");
            pair.getKey().accept(this);
            sb.append(" ");
            sb.append(pair.getValue() ? "DESC" : "ASC");
            first = false;
        }
        return null;
    }

    @Override
    public Void visit(Limit limit) {
        sb.append(" LIMIT ");
        limit.getLimitExpression().accept(this);
        if (limit.getOffsetExpression() != null) {
            sb.append(" OFFSET ");
            limit.getOffsetExpression().accept(this);
        }
        return null;
    }

    //*************************************** visit expression **********************************************

    @Override
    public Void visit(BetweenExpression node) {
        sb.append("(");
        node.getTarget().accept(this);
        sb.append(" BETWEEN ");
        node.getLeft().accept(this);
        sb.append(" AND ");
        node.getRight().accept(this);
        sb.append(")");
        return null;
    }

    @Override
    public Void visit(BinaryExpression node) {
        sb.append("(");
        node.getLeft().accept(this);
        sb.append(" ").append(node.getOp()).append(" ");
        node.getRight().accept(this);
        sb.append(")");
        return null;
    }

    @Override
    public Void visit(FunctionCallExpression node) {
        sb.append(node.getFunctionName()).append("(");
        boolean first = true;
        for (Expression argument : node.getArgumentList()) {
            if (!first) sb.append(", ");
            argument.accept(this);
            first = false;
        }
        sb.append(")");
        return null;
    }

    @Override
    public Void visit(InListExpression node) {
        sb.append("(");
        node.getTarget().accept(this);

        if (node.isNot()) sb.append(" NOT");
        sb.append(" IN ");

        sb.append("(");
        appendExpressionList(node.getList());
        sb.append(")");

        sb.append(")");
        return null;
    }

    @Override
    public Void visit(InSubQueryExpression node) {
        sb.append("(");
        node.getTarget().accept(this);

        if (node.isNot()) sb.append(" NOT");
        sb.append(" IN ");

        sb.append("(");
        node.getSubQuery().accept(this);
        sb.append(")");

        sb.append(")");
        return null;
    }

    @Override
    public Void visit(IntExpression node) {
        sb.append(node.getValue());
        return null;
    }

    @Override
    public Void visit(DoubleExpression node) {
        sb.append(node.getValue());
        return null;
    }

    @Override
    public Void visit(StringExpression node) {
        sb.append("'").append(node.getText()).append("'");
        return null;
    }

    @Override
    public Void visit(NullExpression node) {
        sb.append("NULL");
        return null;
    }

    @Override
    public Void visit(ColumnNameExpression node) {
        if (node.getTableName() != null) {
            sb.append(node.getTableName()).append(".");
        }
        sb.append(node.getColumnName());
        return null;
    }

    @Override
    public Void visit(WildcardExpression node) {
        if (node.getTableName() != null) {
            sb.append(node.getTableName()).append(".");
        }
        sb.append("*");
        return null;
    }

    @Override
    public Void visit(IsNullExpression node) {
        sb.append("(");
        node.getExpression().accept(this);
        sb.append(" IS ");
        if (node.isNot()) sb.append("NOT ");
        sb.append("NULL");
        sb.append(")");
        return null;
    }

    @Override
    public Void visit(LikeExpression node) {
        sb.append("(");
        node.getLeft().accept(this);
        if (node.isNot()) sb.append(" NOT");

        sb.append(" LIKE ");
        node.getRight().accept(this);
        sb.append(")");
        return null;
    }

    @Override
    public Void visit(UnaryExpression node) {
        sb.append("(");
        sb.append(node.getOp());
        node.getExpression().accept(this);
        sb.append(")");
        return null;
    }

    @Override
    public Void visit(AndExpression node) {
        sb.append("(");
        node.getLeft().accept(this);
        sb.append(" AND ");
        node.getRight().accept(this);
        sb.append(")");
        return null;
    }

    @Override
    public Void visit(OrExpression node) {
        sb.append("(");
        node.getLeft().accept(this);
        sb.append(" OR ");
        node.getRight().accept(this);
        sb.append(")");
        return null;
    }

    @Override
    public Void visit(NotExpression node) {
        sb.append("(");
        sb.append("NOT ");
        node.getExpression().accept(this);
        sb.append(")");
        return null;
    }

    @Override
    public Void visit(SubQueryExpression node) {
        if (node.isExists()) {
            sb.append(" EXISTS ");
        }
        sb.append("(");
        node.getSubQuery().accept(this);
        sb.append(")");
        return null;
    }
}
