package com.github.afkbrb.sql;

import com.github.afkbrb.sql.ast.expressions.*;
import com.github.afkbrb.sql.ast.statements.*;

public interface ASTVisitor {

    void visit(CreateStatement node);

    void visit(DeleteStatement node);

    void visit(DropStatement node);

    void visit(InsertStatement node);

    void visit(SelectStatement node);

    void visit(UpdateStatement node);


    void visit(BetweenExpression node);

    void visit(BinaryExpression node);

    void visit(DoubleExpression node);

    void visit(FunctionCallExpression node);

    void visit(IdentifierExpression node);

    void visit(InListExpression node);

    void visit(InSubQueryExpression node);

    void visit(IntExpression node);

    void visit(IsNullExpression node);

    void visit(LikeExpression node);

    void visit(TextExpression node);

    void visit(UnaryExpression node);

    void visit(OrExpression node);

    void visit(NotExpression node);

    void visit(AndExpression node);

    void visit(SelectStatement.TableReferenceFactor node);

    void visit(SelectStatement.TableJoin node);

    void visit(SelectStatement.SubQueryFactor node);

    void visit(SelectStatement.RealTableFactor node);

}
