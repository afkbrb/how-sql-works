package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.ast.expressions.*;
import com.github.afkbrb.sql.ast.statements.*;
import com.github.afkbrb.sql.ast.statements.SelectStatement.*;

public interface Visitor<T> {

    T visit(CreateStatement node);

    T visit(DeleteStatement node);

    T visit(DropStatement node);

    T visit(InsertStatement node);

    T visit(SelectStatement node);

    T visit(UpdateStatement node);


    T visit(BetweenExpression node);

    T visit(BinaryExpression node);

    T visit(FunctionCallExpression node);

    T visit(IdentifierExpression node);

    T visit(InListExpression node);

    T visit(InSubQueryExpression node);

    T visit(IntExpression node);

    T visit(DoubleExpression node);

    T visit(TextExpression node);

    T visit(NullExpression node);

    T visit(IsNullExpression node);

    T visit(LikeExpression node);

    T visit(UnaryExpression node);

    T visit(OrExpression node);

    T visit(NotExpression node);

    T visit(AndExpression node);

    T visit(TableJoin node);

    T visit(SubQueryFactor node);

    T visit(RealTableFactor node);

    T visit(GroupBy groupBy);

    T visit(OrderBy orderBy);

    T visit(Limit limit);
}
