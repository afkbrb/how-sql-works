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

    @NotNull
    public TypedValue evaluate(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public TypedValue visit(BetweenExpression node) {
        TypedValue target = node.getTarget().accept(this);
        TypedValue left = node.getLeft().accept(this);
        TypedValue right = node.getRight().accept(this);
        if (isErrorOrNull(target)) return target;
        if (isErrorOrNull(left)) return left;
        if (isErrorOrNull(right)) return right;
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

        return new EvaluateError("expected three strings or three numbers");
    }

    @Override
    public TypedValue visit(BinaryExpression node) {
        TypedValue left = node.getLeft().accept(this);
        TypedValue right = node.getRight().accept(this);
        if (isErrorOrNull(left)) return left;
        if (isErrorOrNull(right)) return right;
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

        return new EvaluateError("expected two numbers or two strings");
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public TypedValue visit(InSubQueryExpression node) {
        TypedValue target = node.getTarget().accept(this);
        if (isError(target)) return target;
        SelectStatement subQuery = node.getSubQuery();
        try {
            Table table = doSubQuery(subQuery);
            if (table.getColumnCount() == 1) {
                List<Row> rows = table.getRows();
                List<TypedValue> typedValueList = new ArrayList<>();
                for (Row row : rows) {
                    TypedValue typedValue = row.getCell(0).getTypedValue();
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public TypedValue visit(SubQueryExpression node) {
        SelectStatement subQuery = node.getSubQuery();
        try {
            Table table = doSubQuery(subQuery);
            if (node.isExists()) {
                return new TypedValue(INT, toInt(table.getRowCount() > 0));
            } else {
                if (table.getRowCount() == 0) return TypedValue.NULL; // 0 行不报错，返回 NULL
                if (table.getRowCount() == 1 && table.getColumnCount() == 1) {
                    return table.getRow(0).getCell(0).getTypedValue();
                } else {
                    return new EvaluateError("sub query must return exactly 1 column and 0/1 row");
                }
            }
        } catch (SQLExecuteException e) {
            return new EvaluateError(e.getMessage());
        }
    }

    private Table doSubQuery(SelectStatement subQuery) throws SQLExecuteException {
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
        return table;
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

        return new EvaluateError("expected a string or a number");
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public TypedValue visit(ColumnNameExpression node) {
        try {
            Column column;
            if (node.getTableName() == null) {
                column = schema.getColumn(node.getColumnName());
            } else {
                column = schema.getColumn(node.getTableName(), node.getColumnName());
            }
            if (column != null) {
                return row.getCell(column.getColumnIndex()).getTypedValue();
            }

            if (context != null) {
                TypedValue typedValue;
                if (node.getTableName() == null) {
                    typedValue = context.getTypedValue(node.getColumnName());
                } else {
                    typedValue = context.getTypedValue(node.getTableName(), node.getColumnName());
                }
                if (typedValue != null) return typedValue;
            }
        } catch (SQLExecuteException e) {
            return new EvaluateError(e.getMessage());
        }

        return new EvaluateError("cannot find column with name %s", node);
    }

    @Override
    public TypedValue visit(WildcardExpression node) {
        return new EvaluateError("unexpected wildcard expression %s", node.toString());
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
        if (isErrorOrNull(left)) return left;
        if (isErrorOrNull(right)) return right;
        if (isString(left) && isString(right)) {
            String leftStr = (String) left.getValue();
            String rightStr = (String) right.getValue();
            for (int i = 0; i < rightStr.length(); i++) {
                if (rightStr.charAt(i) == '\\') { // \ 只能以 \\ \_ \% d的形式出现
                    if (i + 1 < rightStr.length()) {
                        char next = rightStr.charAt(i + 1);
                        if (next == '\\' || next == '_' || next == '%') {
                            i++;
                        } else {
                            return new EvaluateError("invalid escape '\\%c'", next);
                        }
                    } else {
                        return new EvaluateError("the character to be escaped is missing");
                    }
                }
            }
            try {
                boolean result = like(leftStr, rightStr);
                return new TypedValue(INT, toInt(node.isNot() != result));
            } catch (Exception e) {
                return new EvaluateError(e.getMessage());
            }
        }
        return new EvaluateError("like expects two strings");
    }

    public static boolean like(String left, String right) {
        // 调用时保证了 right 是合法的
        String regex = right.replace("\\", "\\\\")
                .replace(".", "\\.")
                .replace("*", "\\*")
                .replaceAll("(?<!\\\\\\\\)_", ".")
                .replaceAll("(?<!\\\\\\\\)%", ".*")
                .replace("\\\\_", "_")
                .replace("\\\\%", "%")
                .replace("\\\\\\\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("^", "\\^")
                .replace("$", "\\$")
                .replace("?", "\\?")
                .replace("+", "\\+");
        return left.matches(regex); // 完全匹配，符合 like 语义
    }

    @Override
    public TypedValue visit(RegexpExpression node) {
        TypedValue left = node.getLeft().accept(this);
        TypedValue right = node.getRight().accept(this);
        if (isErrorOrNull(left)) return left;
        if (isErrorOrNull(right)) return right;
        if (isString(left) && isString(right)) {
            String leftStr = (String) left.getValue();
            String rightStr = (String) right.getValue();
            // 按 SQL 规范，应该局部匹配
            // 而 Java match 函数使用完全匹配，所以此处加上 .* 解决此问题
            // 当用户想要完全匹配时，输入 xxx regexp '^xxx$' 即可
            String regex = ".*" + rightStr + ".*";
            try {
                boolean result = leftStr.matches(regex);
                return new TypedValue(INT, toInt(node.isNot() != result));
            } catch (Exception e) {
                return new EvaluateError(e.getMessage());
            }
        }
        return new EvaluateError("regexp expects two strings");
    }

    @Override
    public TypedValue visit(UnaryExpression node) {
        TypedValue typedValue = node.getExpression().accept(this);
        if (isErrorOrNull(typedValue)) return typedValue;
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
        return new EvaluateError("expected a number");
    }

    @Override
    public TypedValue visit(AndExpression node) {
        // 逻辑短路优化
        TypedValue left = node.getLeft().accept(this);
        if (isErrorOrNull(left)) return left;
        if (isNumber(left)) {
            double leftValue = ((Number) left.getValue()).doubleValue();
            if (leftValue == 0) return new TypedValue(INT, 0);
        } else {
            return new EvaluateError("expected two numbers");
        }

        TypedValue right = node.getRight().accept(this);
        if (isErrorOrNull(right)) return right;
        if (isNumber(right)) {
            double rightValue = ((Number) right.getValue()).doubleValue();
            return new TypedValue(INT, toInt(rightValue != 0));
        } else {
            return new EvaluateError("expected two numbers");
        }
    }

    @Override
    public TypedValue visit(OrExpression node) {
        // 逻辑短路优化
        TypedValue left = node.getLeft().accept(this);
        if (isErrorOrNull(left)) return left;
        if (isNumber(left)) {
            double leftValue = ((Number) left.getValue()).doubleValue();
            if (leftValue != 0) return new TypedValue(INT, 1);
        } else {
            return new EvaluateError("expected two numbers");
        }

        TypedValue right = node.getRight().accept(this);
        if (isErrorOrNull(right)) return right;
        if (isNumber(right)) {
            double rightValue = ((Number) right.getValue()).doubleValue();
            return new TypedValue(INT, toInt(rightValue != 0));
        } else {
            return new EvaluateError("expected two numbers");
        }
    }

    @Override
    public TypedValue visit(NotExpression node) {
        TypedValue typedValue = node.getExpression().accept(this);
        if (isErrorOrNull(typedValue)) return typedValue;
        if (isNumber(typedValue)) {
            double value = ((Number) typedValue.getValue()).doubleValue();
            return new TypedValue(INT, toInt(value == 0));
        }
        return new EvaluateError("expected a number");
    }

    protected static int toInt(boolean isTrue) {
        return isTrue ? 1 : 0;
    }

}
