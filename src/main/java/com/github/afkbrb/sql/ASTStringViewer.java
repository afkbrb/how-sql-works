package com.github.afkbrb.sql;

import com.github.afkbrb.sql.ast.ASTNode;
import com.github.afkbrb.sql.ast.expressions.*;
import com.github.afkbrb.sql.ast.statements.*;
import com.github.afkbrb.sql.utils.Pair;

public class ASTStringViewer implements ASTVisitor {

    private final StringBuilder sb = new StringBuilder();

    private final ASTNode root;

    public ASTStringViewer(ASTNode root) {
        this.root = root;
    }

    public String getASTString() {
        if (sb.length() == 0) {
            root.accept(this);
            sb.append(";");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getASTString();
    }

    @Override
    public void visit(CreateStatement node) {
        sb.append("CREATE TABLE ").append(node.getTableName());

        sb.append("(");
        boolean first = true;
        for (CreateStatement.ColumnDefinition columnDefinition : node.getColumnDefinitionList()) {
            if (!first) sb.append(", ");
            sb.append(columnDefinition.getColumnName()).append(" ").append(columnDefinition.getColumnType());
            first = false;
        }
        sb.append(")");
    }

    @Override
    public void visit(DeleteStatement node) {
        sb.append("DELETE FROM ").append(node.getTableName());
        if (node.getWhereCondition() != null) {
            sb.append(" WHERE ");
            node.getWhereCondition().accept(this);
        }
    }

    @Override
    public void visit(DropStatement node) {
        sb.append("DROP TABLE ").append(node.getTableName());
    }

    @Override
    public void visit(InsertStatement node) {
        sb.append("INSERT INTO ").append(node.getTableName()).append(" ");
        if (node.getColumnList() != null && node.getColumnList().size() > 0) {
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
    }

    @Override
    public void visit(SelectStatement node) {
        sb.append("SELECT ");

        if (node.getSelectOption() != null) {
            sb.append(node.getSelectOption()).append(" ");
        }

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

        if (node.getGroupByExpression() != null) {
            sb.append(" GROUP BY ");
            node.getGroupByExpression().accept(this);
        }

        if (node.getHavingCondition() != null) {
            sb.append(" HAVING ");
            node.getHavingCondition().accept(this);
        }

        if (node.getOrderBy() != null) {
            sb.append(" ORDER BY ");
            node.getOrderBy().getOrderByExpression().accept(this);
            sb.append(" ").append(node.getOrderBy().isDesc() ? "DESC" : "ASC");
        }

        if (node.getLimit() != null) {
            sb.append(" LIMIT ");
            node.getLimit().getLimitExpression().accept(this);
            if (node.getLimit().getOffsetExpression() != null) {
                sb.append(" OFFSET ");
                node.getLimit().getOffsetExpression().accept(this);
            }
        }
    }

    @Override
    public void visit(UpdateStatement node) {
        sb.append("UPDATE ").append(node.getTableName()).append(" SET ");
        boolean first = true;
        for (Pair<String, Expression> pair : node.getSetList()) {
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
    }

    //*************************************** visit expression **********************************************

    @Override
    public void visit(BetweenExpression node) {
        sb.append("(");
        node.getTarget().accept(this);
        sb.append(" BETWEEN ");
        node.getLeft().accept(this);
        sb.append(" AND ");
        node.getRight().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(BinaryExpression node) {
        sb.append("(");
        node.getLeft().accept(this);
        sb.append(" ").append(node.getOp()).append(" ");
        node.getRight().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(DoubleExpression node) {
        sb.append(node.getValue());
    }

    @Override
    public void visit(FunctionCallExpression node) {
        sb.append(node.getFunctionName()).append("(");
        boolean first = true;
        for (Expression argument : node.getArgumentList()) {
            if (!first) sb.append(", ");
            argument.accept(this);
            first = false;
        }
        sb.append(")");
    }

    @Override
    public void visit(IdentifierExpression node) {
        sb.append(node.getIdentifier());
    }

    @Override
    public void visit(InListExpression node) {
        sb.append("(");
        node.getLeft().accept(this);

        if (node.isNot()) sb.append(" NOT");
        sb.append(" IN ");

        sb.append("(");
        boolean first = true;
        for (Expression expression : node.getRightList()) {
            if (!first) sb.append(", ");
            expression.accept(this);
            first = false;
        }
        sb.append(")");

        sb.append(")");
    }

    @Override
    public void visit(InSubQueryExpression node) {
        sb.append("(");
        node.getLeft().accept(this);

        if (node.isNot()) sb.append(" NOT");
        sb.append(" IN ");

        sb.append("(");
        node.getRight().accept(this);
        sb.append(")");

        sb.append(")");
    }

    @Override
    public void visit(IntExpression node) {
        sb.append(node.getValue());
    }

    @Override
    public void visit(IsNullExpression node) {
        sb.append("(");
        node.getExpression().accept(this);
        sb.append(" IS ");
        if (node.isNot()) sb.append("NOT ");
        sb.append("NULL");
        sb.append(")");
    }

    @Override
    public void visit(LikeExpression node) {
        sb.append("(");
        node.getLeft().accept(this);
        if (node.isNot()) sb.append(" NOT");

        sb.append(" LIKE ");
        node.getRight().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(TextExpression node) {
        sb.append("'").append(node.getText()).append("'"); // TODO 也许要用双引号
    }

    @Override
    public void visit(UnaryExpression node) {
        sb.append("(");
        sb.append(node.getOp());
        node.getExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(OrExpression node) {
        sb.append("(");
        node.getLeft().accept(this);
        sb.append(" OR ");
        node.getRight().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(NotExpression node) {
        sb.append("(");
        sb.append("NOT ");
        node.getExpression().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(AndExpression node) {
        sb.append("(");
        node.getLeft().accept(this);
        sb.append(" AND ");
        node.getRight().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(SelectStatement.TableReferenceFactor node) {
        if (node.getAlias() != null) sb.append("(");

        sb.append("(");
        node.getTableReference().accept(this);
        sb.append(")");
        if (node.getAlias() != null) {
            sb.append(" AS ").append(node.getAlias());
        }

        if (node.getAlias() != null) sb.append(")");
    }

    @Override
    public void visit(SelectStatement.TableJoin node) {
        sb.append("(");
        node.getLeft().accept(this);
        sb.append(" ").append(node.getJoinType()).append(" JOIN ");
        node.getRight().accept(this);
        if (node.getOn() != null) {
            sb.append(" ON ");
            node.getOn().accept(this);
        }
        sb.append(")");
    }

    @Override
    public void visit(SelectStatement.SubQueryFactor node) {
        if (node.getAlias() != null) sb.append("(");

        sb.append("(");
        node.getSelectStatement().accept(this);
        sb.append(")");

        if (node.getAlias() != null) {
            sb.append(" AS ").append(node.getAlias());
        }

        if (node.getAlias() != null) sb.append(")");
    }

    @Override
    public void visit(SelectStatement.RealTableFactor node) {
        sb.append(node.getTableName());
        if (node.getTableNameAlias() != null) {
            sb.append(" AS ").append(node.getTableNameAlias());
        }
    }
}
