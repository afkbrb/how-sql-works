package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.ast.expressions.*;
import com.github.afkbrb.sql.ast.statements.SelectStatement;
import com.github.afkbrb.sql.executors.SelectExecutor;
import com.github.afkbrb.sql.functions.Function;
import com.github.afkbrb.sql.functions.FunctionRegistry;
import com.github.afkbrb.sql.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.github.afkbrb.sql.model.DataType.*;

/**
 * 类型推导器
 * <p>
 * 推导建立在表达式合法的基础上，如果非法的话会在实际执行时报错。
 */
public class TypeInferer extends DefaultVisitor<DataType> {

    private final InheritedContext context;
    private final Schema schema;

    public TypeInferer(@NotNull InheritedContext context, @NotNull Schema schema) {
        this.context = Objects.requireNonNull(context);
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
    public DataType visit(StringExpression node) {
        return STRING;
    }

    @Override
    public DataType visit(NullExpression node) {
        return NULL;
    }

    @Override
    public DataType visit(ColumnNameExpression node) {
        try {
            if (node.getTableName() == null) {
                Column column = schema.getColumn(node.getColumnName());
                if (column == null) {
                    TypedValue typedValue = context.getTypedValue(node.getColumnName());
                    if (typedValue == null) return ERROR;
                    else return typedValue.getDataType();
                } else {
                    return column.getDataType();
                }
            } else {
                Column column = schema.getColumn(node.getTableName(), node.getColumnName());
                if (column == null) {
                    TypedValue typedValue = context.getTypedValue(node.getTableName(), node.getColumnName());
                    if (typedValue == null) return ERROR;
                    else return typedValue.getDataType();
                } else {
                    return column.getDataType();
                }
            }
        } catch (SQLExecuteException e) {
            // e.printStackTrace();
            return ERROR;
        }
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
    public DataType visit(RegexpExpression node) {
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

    @Override
    public DataType visit(SubQueryExpression node) {
        if (node.isExists()) return INT;
        SelectStatement subQuery = node.getSubQuery();
        if (subQuery.getSelectItemList().size() != 1) return ERROR;

        // 此处通过实际执行子查询类获取类型，为了降低开销，将 where 条件设为 0，
        // 这样的话执行时实际上就只完成了 schema 的构建，开销非常小
        SelectStatement simplifiedSubQuery = new SelectStatement(subQuery.getSelectItemList(), subQuery.getTableReference(),
                new IntExpression(0), null, null, null);
        try {
            Table table = new SelectExecutor(context).doSelect(simplifiedSubQuery);
            return table.getSchema().getColumns().get(0).getDataType();
        } catch (SQLExecuteException e) {
            return ERROR;
        }
    }
}
