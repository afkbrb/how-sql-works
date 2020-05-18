package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.ast.expressions.*;
import com.github.afkbrb.sql.functions.Function;
import com.github.afkbrb.sql.functions.FunctionRegistry;
import com.github.afkbrb.sql.model.DataType;
import com.github.afkbrb.sql.model.Schema;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.github.afkbrb.sql.model.DataType.*;

/**
 * 类型推导器
 *
 * 推导建立在表达式合法的基础上，如果非法的话会在实际执行时报错。
 */
public class TypeInferer extends DefaultVisitor<DataType> {

    private final Schema schema;

    public TypeInferer(@NotNull Schema schema) {
        this.schema = Objects.requireNonNull(schema);
    }

    public DataType infer(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public DataType visit(BetweenExpression node) {
        return INT;
    }

    @Override
    public DataType visit(BinaryExpression node) {
        DataType leftType = node.getLeft().accept(this);
        DataType rightType = node.getRight().accept(this);
        return leftType == INT && rightType == INT ? INT : DOUBLE;
    }

    @Override
    public DataType visit(FunctionCallExpression node) {
        Function function = FunctionRegistry.getFunction(node.getFunctionName());
        return function == null ? ERROR : function.getReturnType();
    }

    @Override
    public DataType visit(IdentifierExpression node) {
        DataType columnType = schema.getColumnType(node.getIdentifier());
        return columnType == null ? ERROR : columnType;
    }

    @Override
    public DataType visit(InListExpression node) {
        return INT;
    }

    @Override
    public DataType visit(InSubQueryExpression node) {
        return INT;
    }

    @Override
    public DataType visit(IntExpression node) {
        return INT;
    }

    @Override
    public DataType visit(DoubleExpression node) {
        return DOUBLE;
    }

    @Override
    public DataType visit(TextExpression node) {
        return STRING;
    }

    @Override
    public DataType visit(NullExpression node) {
        return NULL;
    }

    @Override
    public DataType visit(IsNullExpression node) {
        return INT;
    }

    @Override
    public DataType visit(LikeExpression node) {
        return INT;
    }

    @Override
    public DataType visit(UnaryExpression node) {
        return node.getExpression().accept(this);
    }

    @Override
    public DataType visit(AndExpression node) {
        return INT;
    }

    @Override
    public DataType visit(OrExpression node) {
        return INT;
    }

    @Override
    public DataType visit(NotExpression node) {
        return INT;
    }

}
