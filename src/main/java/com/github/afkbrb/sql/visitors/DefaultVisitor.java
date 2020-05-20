package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.ast.expressions.*;
import com.github.afkbrb.sql.ast.statements.*;

/**
 * 会访问所有 children，但不会使用访问结果。
 */
public class DefaultVisitor<T> implements Visitor<T> {

    @Override
    public T visit(CreateStatement node) {
        return null;
    }

    @Override
    public T visit(DeleteStatement node) {
        if (node.getWhereCondition() != null) {
            node.getWhereCondition().accept(this);
        }
        return null;
    }

    @Override
    public T visit(DropStatement node) {
        return null;
    }

    @Override
    public T visit(InsertStatement node) {
        node.getValueList().forEach(expression -> expression.accept(this));
        return null;
    }

    @Override
    public T visit(SelectStatement node) {
        node.getSelectItemList().forEach(pair -> pair.getKey().accept(this));
        if (node.getTableReference() != null) {
            node.getTableReference().accept(this);
        }
        if (node.getWhereCondition() != null) {
            node.getWhereCondition().accept(this);
        }
        if (node.getGroupBy() != null) {
            node.getGroupBy().accept(this);
        }
        if (node.getOrderBy() != null) {
            node.getOrderBy().accept(this);
        }
        if (node.getLimit() != null) {
            node.getLimit().accept(this);
        }
        return null;
    }

    @Override
    public T visit(UpdateStatement node) {
        node.getUpdateList().forEach(pair -> pair.getValue().accept(this));
        if (node.getWhereCondition() != null) {
            node.getWhereCondition().accept(this);
        }
        return null;
    }

    @Override
    public T visit(SelectStatement.TableJoin node) {
        node.getLeft().accept(this);
        node.getRight().accept(this);
        if (node.getOn() != null) {
            node.getOn().accept(this);
        }
        return null;
    }

    @Override
    public T visit(SelectStatement.DerivedTable node) {
        node.getSelectStatement().accept(this);
        return null;
    }

    @Override
    public T visit(SelectStatement.RealTableFactor node) {
        return null;
    }

    @Override
    public T visit(SelectStatement.GroupBy groupBy) {
        groupBy.getGroupByList().forEach(expression -> expression.accept(this));
        if (groupBy.getHavingCondition() != null) {
            groupBy.getHavingCondition().accept(this);
        }
        return null;
    }

    @Override
    public T visit(SelectStatement.OrderBy orderBy) {
        orderBy.getOrderByList().forEach(pair -> pair.getKey().accept(this));
        return null;
    }

    @Override
    public T visit(SelectStatement.Limit limit) {
        limit.getLimitExpression().accept(this);
        if (limit.getOffsetExpression() != null) {
            limit.getOffsetExpression().accept(this);
        }
        return null;
    }

    //******************************************** expression ***************************************************

    @Override
    public T visit(BetweenExpression node) {
        node.getTarget().accept(this);
        node.getLeft().accept(this);
        node.getRight().accept(this);
        return null;
    }

    @Override
    public T visit(BinaryExpression node) {
        node.getLeft().accept(this);
        node.getRight().accept(this);
        return null;
    }

    @Override
    public T visit(FunctionCallExpression node) {
        node.getArgumentList().forEach(expression -> expression.accept(this));
        return null;
    }

    @Override
    public T visit(InListExpression node) {
        node.getTarget().accept(this);
        node.getList().forEach(expression -> expression.accept(this));
        return null;
    }

    @Override
    public T visit(InSubQueryExpression node) {
        node.getTarget().accept(this);
        node.getSubQuery().accept(this);
        return null;
    }

    @Override
    public T visit(IntExpression node) {
        return null;
    }

    @Override
    public T visit(DoubleExpression node) {
        return null;
    }

    @Override
    public T visit(StringExpression node) {
        return null;
    }

    @Override
    public T visit(NullExpression node) {
        return null;
    }

    @Override
    public T visit(ColumnNameExpression node) {
        return null;
    }

    @Override
    public T visit(WildcardExpression node) {
        return null;
    }

    @Override
    public T visit(IsNullExpression node) {
        node.getExpression().accept(this);
        return null;
    }

    @Override
    public T visit(LikeExpression node) {
        node.getLeft().accept(this);
        node.getRight().accept(this);
        return null;
    }

    @Override
    public T visit(UnaryExpression node) {
        node.getExpression().accept(this);
        return null;
    }

    @Override
    public T visit(AndExpression node) {
        node.getLeft().accept(this);
        node.getRight().accept(this);
        return null;
    }

    @Override
    public T visit(SubQueryExpression node) {
        node.getSubQuery().accept(this);
        return null;
    }

    @Override
    public T visit(OrExpression node) {
        node.getLeft().accept(this);
        node.getRight().accept(this);
        return null;
    }

    @Override
    public T visit(NotExpression node) {
        node.getExpression().accept(this);
        return null;
    }

}
