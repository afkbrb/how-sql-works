package com.github.afkbrb.sql.visitors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.ast.expressions.*;
import com.github.afkbrb.sql.ast.statements.SelectStatement;
import com.github.afkbrb.sql.executors.SelectExecutor;
import com.github.afkbrb.sql.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.github.afkbrb.sql.model.DataType.*;
import static com.github.afkbrb.sql.utils.DataTypeUtils.*;


public abstract class AbstractEvaluator extends DefaultVisitor<TypedValue> {

    protected final InheritedContext context;
    protected final Row row;
    protected final Schema schema;

    public AbstractEvaluator(@Nullable InheritedContext context, @NotNull Schema schema, @NotNull Row row) {
        this.context = context;
        this.schema = schema;
        this.row = row;
    }

    public TypedValue evaluate(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public TypedValue visit(BetweenExpression node) {
        TypedValue target = node.getTarget().accept(this);
        TypedValue left = node.getLeft().accept(this);
        TypedValue right = node.getRight().accept(this);
        if (isError(target)) return target;
        if (isError(left)) return left;
        if (isError(right)) return right;

        if (isString(target) && isString(left) && isString(right)) {
            String targetSt = (String) target.getValue();
            String leftStr = (String) left.getValue();
            String rightStr = (String) right.getValue();
            return new TypedValue(INT, toInt(targetSt.compareTo(leftStr) >= 0 && targetSt.compareTo(rightStr) <= 0));
        } else if (isNumber(target) && isNumber(left) && isNumber(right)) {
            double targetVal = ((Number) target.getValue()).doubleValue();
            double leftVal = ((Number) left.getValue()).doubleValue();
            double rightVal = ((Number) right.getValue()).doubleValue();
            return new TypedValue(INT, toInt(targetVal >= leftVal && targetVal <= rightVal));
        }

        return new EvaluateError("expect three strings or three numbers");
    }

    @Override
    public TypedValue visit(BinaryExpression node) {
        TypedValue left = node.getLeft().accept(this);
        TypedValue right = node.getRight().accept(this);
        if (isError(left)) return left;
        if (isError(right)) return right;
        if (isNumber(left) && isNumber(right)) {
            double leftVal = ((Number) left.getValue()).doubleValue();
            double rightVal = ((Number) right.getValue()).doubleValue();
            double result;
            switch (node.getOp()) {
                case ADD:
                    result = leftVal + rightVal;
                    break;
                case MINUS:
                    result = leftVal - rightVal;
                    break;
                case MULT:
                    result = leftVal * rightVal;
                    break;
                case DIV:
                    if (rightVal == 0) return new EvaluateError("division by 0");
                    result = leftVal / rightVal;
                    break;
                case GT:
                    result = toInt(leftVal > rightVal);
                    break;
                case LT:
                    result = toInt(leftVal < rightVal);
                    break;
                case EQ:
                    result = toInt(leftVal == rightVal);
                    break;
                case LE:
                    result = toInt(leftVal <= rightVal);
                    break;
                case GE:
                    result = toInt(leftVal >= rightVal);
                    break;
                case NE:
                    result = toInt(leftVal != rightVal);
                    break;
                default:
                    return new EvaluateError("unexpected binary operator %s", node.getOp().name());
            }

            if (isInt(left) && isInt(right)) {
                return new TypedValue(INT, (int) result);
            } else {
                return new TypedValue(DOUBLE, result);
            }
        } else if (isString(left) && isString(right)) {
            String leftStr = (String) left.getValue();
            String rightStr = (String) right.getValue();
            switch (node.getOp()) {
                case ADD:
                case MINUS:
                case MULT:
                case DIV:
                    return new EvaluateError("operator %s cannot be used on string", node.getOp());
                case LT:
                    return new TypedValue(INT, toInt(leftStr.compareTo(rightStr) < 0));
                case GT:
                    return new TypedValue(INT, toInt(leftStr.compareTo(rightStr) > 0));
                case EQ:
                    return new TypedValue(INT, toInt(leftStr.compareTo(rightStr) == 0));
                case LE:
                    return new TypedValue(INT, toInt(leftStr.compareTo(rightStr) <= 0));
                case GE:
                    return new TypedValue(INT, toInt(leftStr.compareTo(rightStr) >= 0));
                case NE:
                    return new TypedValue(INT, toInt(leftStr.compareTo(rightStr) != 0));
            }
        }

        return new EvaluateError("expect two numbers ro two strings");
    }

    @Override
    public abstract TypedValue visit(FunctionCallExpression node);

    @Override
    public TypedValue visit(InListExpression node) {
        TypedValue target = node.getTarget().accept(this);
        if (isError(target)) return target;
        List<TypedValue> typedValueList = new ArrayList<>();
        for (Expression expression : node.getList()) {
            TypedValue typedValue = expression.accept(this);
            if (isError(typedValue)) return typedValue;
            typedValueList.add(typedValue);
        }

        return inList(target, typedValueList);
    }

    @Override
    public TypedValue visit(InSubQueryExpression node) {
        TypedValue target = node.getTarget().accept(this);
        SelectStatement subQuery = node.getSubQuery();
        try {
            Table table;
            if (context != null) {
                context.push(schema, row);
                table = new SelectExecutor(context).doSelect(subQuery);
                context.pop();
            } else {
                InheritedContext context = new InheritedContext();
                context.push(schema, row);
                table = new SelectExecutor(context).doSelect(subQuery);
            }
            if (table.getColumnCount() == 1) {
                List<Row> rows = table.getRows();
                List<TypedValue> typedValueList = new ArrayList<>();
                for (Row row : rows) {
                    Cell cell = row.getCell(0);
                    assert cell != null;
                    TypedValue typedValue = cell.getTypedValue();
                    if (isError(typedValue)) return typedValue;
                    typedValueList.add(typedValue);
                }
                return inList(target, typedValueList);
            } else {
                return new EvaluateError("Sub query should contain exactly one column");
            }
        } catch (SQLExecuteException e) {
            return new EvaluateError(e.getMessage());
        }
    }

    private static TypedValue inList(TypedValue target, List<TypedValue> typedValueList) {
        if (isString(target)) {
            String targetStr = (String) target.getValue();
            for (TypedValue typedValue : typedValueList) {
                if (isString(typedValue) && targetStr.equals(typedValue.getValue())) {
                    return new TypedValue(INT, 1);
                }
            }
            return new TypedValue(INT, 0);
        } else if (isNumber(target)) {
            double targetVal = ((Number) target.getValue()).doubleValue();
            for (TypedValue typedValue : typedValueList) {
                if (isNumber(typedValue) && targetVal == ((Number) typedValue.getValue()).doubleValue()) {
                    return new TypedValue(INT, 1);
                }
            }
            return new TypedValue(INT, 0);
        }

        return new EvaluateError("expect a string or a number");
    }

    @Override
    public TypedValue visit(IntExpression node) {
        return new TypedValue(INT, node.getValue());
    }

    @Override
    public TypedValue visit(DoubleExpression node) {
        return new TypedValue(DOUBLE, node.getValue());
    }

    @Override
    public TypedValue visit(StringExpression node) {
        return new TypedValue(STRING, node.getText());
    }

    @Override
    public TypedValue visit(NullExpression node) {
        return TypedValue.NULL;
    }

    @Override
    public TypedValue visit(ColumnNameExpression node) {
        Column column;
        try {
            if (node.getTableName() == null) {
                column = schema.getColumn(node.getColumnName());
            } else {
                column = schema.getColumn(node.getTableName(), node.getColumnName());
            }
            if (column != null) {
                Cell cell = row.getCell(column.getColumnIndex());
                assert cell != null;
                return cell.getTypedValue();
            }

            if (context != null) {
                if (node.getTableName() == null) {
                    return context.getTypedValue(node.getColumnName());
                } else {
                    return context.getTypedValue(node.getTableName(), node.getColumnName());
                }
            }
        } catch (SQLExecuteException e) {
            return new EvaluateError(e.getMessage());
        }

        return new EvaluateError("Cannot find column with name %s", node);
    }

    @Override
    public TypedValue visit(WildcardExpression node) {
        return new EvaluateError("Unexpected wildcard expression %s", node);
    }

    @Override
    public TypedValue visit(IsNullExpression node) {
        TypedValue typedValue = node.getExpression().accept(this);
        if (isError(typedValue)) return typedValue;
        if (node.isNot()) {
            return new TypedValue(INT, toInt(typedValue.getDataType() != NULL));
        } else {
            return new TypedValue(INT, toInt(typedValue.getDataType() == NULL));
        }
    }

    @Override
    public TypedValue visit(LikeExpression node) {
        TypedValue left = node.getLeft().accept(this);
        TypedValue right = node.getRight().accept(this);
        if (isError(left)) return left;
        if (isError(right)) return right;

        if (isString(left) && isString(right)) {
            String leftStr = (String) left.getValue();
            String rightStr = (String) right.getValue();
            // 将 rightStr 转化成等价的正则表达式就行了
            // 当然，还得注意转义
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rightStr.length(); i++) {
                char curr = rightStr.charAt(i);
                if (curr == '\\') {
                    if (i + 1 < rightStr.length()) {
                        char next = rightStr.charAt(++i);
                        if (next == '_' || next == '%') {
                            sb.append(next);
                        } else {
                            sb.append('\\').append(next);
                        }
                    } else {
                        sb.append('\\');
                    }
                } else if (curr == '_') {
                    sb.append(".");
                } else if (curr == '%') {
                    sb.append(".*");
                } else {
                    sb.append(curr);
                }
            }
            String regex = sb.toString();
            if (node.isNot()) {
                return new TypedValue(INT, toInt(!leftStr.matches(regex)));
            } else {
                return new TypedValue(INT, toInt(leftStr.matches(regex)));
            }
        }
        return new EvaluateError("expect two strings");
    }

    @Override
    public TypedValue visit(UnaryExpression node) {
        TypedValue typedValue = node.getExpression().accept(this);
        if (isError(typedValue)) return typedValue;
        if (isNumber(typedValue)) {
            double value = ((Number) typedValue.getValue()).doubleValue();
            switch (node.getOp()) {
                case ADD:
                    break;
                case MINUS:
                    value = -value;
                    break;
                default:
                    return new EvaluateError("unexpected unary operator %s", node.getOp().name());
            }
            if (isInt(typedValue)) {
                return new TypedValue(INT, (int) value);
            } else {
                return new TypedValue(DOUBLE, value);
            }
        }
        return new EvaluateError("expect a number");
    }

    @Override
    public TypedValue visit(AndExpression node) {
        TypedValue left = node.getLeft().accept(this);
        TypedValue right = node.getRight().accept(this);
        if (isError(left)) return left;
        if (isError(right)) return right;
        if (isNumber(left) && isNumber(right)) {
            double leftVal = ((Number) left.getValue()).doubleValue();
            double rightVal = ((Number) right.getValue()).doubleValue();
            return new TypedValue(INT, leftVal == 0 || rightVal == 0 ? 0 : 1);
        }
        return new EvaluateError("expect two numbers");
    }

    @Override
    public TypedValue visit(OrExpression node) {
        TypedValue left = node.getLeft().accept(this);
        TypedValue right = node.getRight().accept(this);
        if (isError(left)) return left;
        if (isError(right)) return right;
        if (isNumber(left) && isNumber(right)) {
            double leftVal = ((Number) left.getValue()).doubleValue();
            double rightVal = ((Number) right.getValue()).doubleValue();
            return new TypedValue(INT, toInt(leftVal != 0 || rightVal != 0));
        }
        return new EvaluateError("expect two numbers");
    }

    @Override
    public TypedValue visit(NotExpression node) {
        TypedValue typedValue = node.getExpression().accept(this);
        if (isError(typedValue)) return typedValue;
        if (isNumber(typedValue)) {
            double value = ((Number) typedValue.getValue()).doubleValue();
            return new TypedValue(INT, toInt(value == 0));
        }
        return new EvaluateError("expect a number");
    }

    protected static int toInt(boolean isTrue) {
        return isTrue ? 1 : 0;
    }
}
