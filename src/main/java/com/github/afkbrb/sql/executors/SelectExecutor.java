package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.TableManager;
import com.github.afkbrb.sql.ast.expressions.ColumnNameExpression;
import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.ast.expressions.IntExpression;
import com.github.afkbrb.sql.ast.expressions.WildcardExpression;
import com.github.afkbrb.sql.ast.statements.SelectStatement;
import com.github.afkbrb.sql.ast.statements.SelectStatement.*;
import com.github.afkbrb.sql.model.*;
import com.github.afkbrb.sql.utils.ExpressionUtils;
import com.github.afkbrb.sql.utils.Pair;
import com.github.afkbrb.sql.visitors.AggregateDetector;
import com.github.afkbrb.sql.visitors.TypeInferer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.afkbrb.sql.utils.DataTypeUtils.isNull;

public class SelectExecutor extends Executor {

    private final InheritedContext context;

    public SelectExecutor() {
        this(new InheritedContext());
    }

    public SelectExecutor(@NotNull InheritedContext context) {
        this.context = Objects.requireNonNull(context);
    }

    public Table doSelect(SelectStatement selectStatement) throws SQLExecuteException {
        return doSelect(selectStatement, "result");
    }

    /**
     * 处理 select 比较麻烦，需要考虑多种情况。
     */
    public Table doSelect(@NotNull SelectStatement selectStatement, @NotNull String tableName) throws SQLExecuteException {
        Objects.requireNonNull(selectStatement);
        Objects.requireNonNull(tableName);
        List<Pair<Expression, String>> originalSelectItemList = selectStatement.getSelectItemList();

        // 主要是针对 group by 语句和聚集函数的存在与否分类处理
        boolean isGroupBy = selectStatement.getGroupBy() != null;
        boolean isAggregate = false;
        for (Pair<Expression, String> pair : originalSelectItemList) {
            if (isAggregate(pair.getKey())) {
                isAggregate = true;
                break;
            }
        }

        // FROM tableReference
        Table referenceTable = makeReferenceTable(selectStatement.getTableReference());
        Schema schema = referenceTable.getSchema();
        List<Row> rows = new ArrayList<>(referenceTable.getRows());

        if (selectStatement.getWhereCondition() != null) {
            for (Iterator<Row> iterator = rows.iterator(); iterator.hasNext(); ) {
                if (!predicate(context, schema, iterator.next(), selectStatement.getWhereCondition())) iterator.remove();
            }
        }

        // 展开 *，顺便把别名处理了。
        List<Pair<Expression, String>> selectItemList = new ArrayList<>();
        for (Pair<Expression, String> pair : originalSelectItemList) {
            Expression expression = pair.getKey();
            if (expression instanceof WildcardExpression) {
                WildcardExpression wildcardExpression = (WildcardExpression) expression;
                List<Column> columnList = schema.getColumns();
                if (wildcardExpression.getTableName() != null) {
                    // tableName.*
                    columnList = columnList.stream().filter(column -> column.getTableName().equalsIgnoreCase(wildcardExpression.getTableName())).collect(Collectors.toList());
                }
                columnList.forEach(column -> selectItemList.add(new Pair<>(new ColumnNameExpression(column.getTableName(), column.getColumnName()), column.getColumnName())));
            } else {
                // 别名处理
                if (pair.getValue() == null) {
                    // 没有别名就自动生成别名
                    if (expression instanceof ColumnNameExpression && ((ColumnNameExpression) expression).getTableName() != null) {
                        // 如果没有指定别名且列名为 tableName.columnName 的形式
                        selectItemList.add(new Pair<>(expression, ((ColumnNameExpression) expression).getColumnName()));
                    } else {
                        selectItemList.add(new Pair<>(expression, ExpressionUtils.trimParenthesis(expression)));
                    }
                } else {
                    selectItemList.add(new Pair<>(expression, pair.getValue()));
                }
            }
        }

        // 从 selectItemList 中把 expression 单独抽出来，便于后面使用
        List<Expression> selectExpressionList = new ArrayList<>();
        selectItemList.forEach(pair -> selectExpressionList.add(pair.getKey()));

        // 先生成表，再往里面填数据
        List<Column> columnList = new ArrayList<>();
        for (int i = 0; i < selectItemList.size(); i++) {
            Pair<Expression, String> pair = selectItemList.get(i);
            DataType dataType = inferType(schema, pair.getKey());
            // 如果是 derived table 的话，这边的别名会被子其别名覆盖
            Column column = new Column(i, pair.getValue(), dataType, tableName);
            columnList.add(column);
        }
        Table table = new Table(tableName, columnList);

        int limit = Integer.MAX_VALUE;
        int offset = 0;
        if (selectStatement.getLimit() != null) {
            TypedValue evaluatedLimit = evaluate(context, selectStatement.getLimit().getLimitExpression());
            if (!(evaluatedLimit.getValue() instanceof Integer)) {
                throw new SQLExecuteException("expected a integer result of evaluate");
            }
            limit = (int) evaluatedLimit.getValue();
            if (limit < 0) {
                throw new SQLExecuteException("expected limit >= 0");
            }
            if (selectStatement.getLimit().getOffsetExpression() != null) {
                TypedValue evaluatedOffset = evaluate(context, selectStatement.getLimit().getOffsetExpression());
                if (!(evaluatedOffset.getValue() instanceof Integer)) {
                    throw new SQLExecuteException("expected a integer result of evaluate");
                }
                offset = (int) evaluatedOffset.getValue();
                if (offset < 0) {
                    throw new SQLExecuteException("expected offset >= 0");
                }
            }
        }

        if (!isGroupBy && !isAggregate) {
            // 如果需要排序的话，先对 rows 进行排序
            if (selectStatement.getOrderBy() != null) {
                OrderBy orderBy = selectStatement.getOrderBy();
                List<Pair<Expression, Boolean>> orderByList = orderBy.getOrderByList();
                sortRows(rows, schema, orderByList);
            }

            // 填充数据
            int upperBound = Math.min(rows.size(), offset + limit);
            for (int i = offset; i < upperBound; i++) {
                Row row = rows.get(i);
                List<Cell> cells = new ArrayList<>();
                for (Expression expression : selectExpressionList) {
                    TypedValue value = evaluate(context, schema, row, expression);
                    cells.add(new Cell(value));
                }
                table.addRow(new Row(cells));
            }
        } else if (!isGroupBy) {
            // 没有 group by，但使用了聚集函数的话，只有一行记录
            if (!(limit > 0 && offset == 0)) return table;
            List<Cell> cells = new ArrayList<>();
            for (Expression expression : selectExpressionList) {
                cells.add(new Cell(evaluate(context, schema, rows, expression)));
            }
            table.addRow(new Row(cells));
        } else {
            // 有 groupBy 了
            // 分组时，虽然每组可以有多行记录，但是生成表时，一组只对应一条记录，
            // 所以 order by 是针对 group 的，而不是 rows。

            GroupBy groupBy = selectStatement.getGroupBy();
            List<Expression> groupByList = groupBy.getGroupByList();
            Map<Group, List<Row>> groupMap = new HashMap<>();
            for (Row row : rows) {
                List<TypedValue> items = new ArrayList<>();
                for (Expression expression : groupByList) {
                    items.add(evaluate(context, schema, row, expression));
                }
                Group group = new Group(items);
                groupMap.putIfAbsent(group, new ArrayList<>());
                groupMap.get(group).add(row);
            }

            if (groupBy.getHavingCondition() != null) {
                for (Iterator<Map.Entry<Group, List<Row>>> iterator = groupMap.entrySet().iterator(); iterator.hasNext(); ) {
                    if (!predicate(context, schema, iterator.next().getValue(), groupBy.getHavingCondition()))
                        iterator.remove();
                }
            }

            ArrayList<Map.Entry<Group, List<Row>>> groupEntryList = new ArrayList<>(groupMap.entrySet());
            if (selectStatement.getOrderBy() != null) {
                OrderBy orderBy = selectStatement.getOrderBy();
                List<Pair<Expression, Boolean>> orderByList = orderBy.getOrderByList();
                sortGroups(groupEntryList, schema, orderByList);
            }

            int upperBound = Math.min(groupEntryList.size(), offset + limit);
            for (int i = offset; i < upperBound; i++) {
                List<Row> groupRows = groupEntryList.get(i).getValue();
                List<Cell> cells = new ArrayList<>();
                for (Expression expression : selectExpressionList) {
                    TypedValue value = evaluate(context, schema, groupRows, expression);
                    cells.add(new Cell(value));
                }
                table.addRow(new Row(cells));
            }
        }

        return table;
    }

    /**
     * 判断一个 expression 中是否调用了聚集函数
     */
    private boolean isAggregate(Expression expression) {
        AggregateDetector detector = new AggregateDetector(expression);
        return detector.detect();
    }

    /**
     * 推导 expression 的类型
     */
    private DataType inferType(Schema schema, Expression expression) {
        TypeInferer inferer = new TypeInferer(context, schema);
        return inferer.infer(expression);
    }

    public void sortRows(List<Row> rows, Schema schema, List<Pair<Expression, Boolean>> orderByList) throws SQLExecuteException {
        Row[] rowArr = rows.toArray(new Row[0]);

        // 先在外部将 evaluate 的结果计算出来，否则如果在 lambda 内部求值的话，异常没法处理。
        Map<Row, List<TypedValue>> rowToTypedValue = new HashMap<>();
        for (Row row : rows) {
            rowToTypedValue.put(row, new ArrayList<>());
            for (Pair<Expression, Boolean> pair : orderByList) {
                rowToTypedValue.get(row).add(evaluate(context, schema, row, pair.getKey()));
            }
        }

        Arrays.sort(rowArr, (thisRow, otherRow) -> {
            for (int i = 0; i < orderByList.size(); i++) {
                TypedValue thisResult = rowToTypedValue.get(thisRow).get(i);
                TypedValue otherResult = rowToTypedValue.get(otherRow).get(i);
                int cmp = thisResult.compareTo(otherResult);
                System.out.println("cmp" + cmp);
                if (cmp == 0) continue; // 继续比较
                return orderByList.get(i).getValue() ? -cmp : cmp;
            }
            return 0; // 所有都尝试了，仍相等，返回 0
        });
        rows.clear();
        rows.addAll(Arrays.asList(rowArr));
    }

    @SuppressWarnings("unchecked")
    private void sortGroups(List<Map.Entry<Group, List<Row>>> groups, Schema schema, List<Pair<Expression, Boolean>> orderByList) throws SQLExecuteException {
        Map.Entry<Group, List<Row>>[] groupArr = groups.toArray(new Map.Entry[0]);
        Map<Map.Entry<Group, List<Row>>, List<TypedValue>> groupToTypedValue = new HashMap<>();
        for (Map.Entry<Group, List<Row>> groupEntry : groups) {
            groupToTypedValue.put(groupEntry, new ArrayList<>());
            for (Pair<Expression, Boolean> pair : orderByList) {
                groupToTypedValue.get(groupEntry).add(evaluate(context, schema, groupEntry.getValue(), pair.getKey()));
            }
        }

        Arrays.sort(groupArr, (thisGroup, otherGroup) -> {
            for (int i = 0; i < orderByList.size(); i++) {
                TypedValue thisResult = groupToTypedValue.get(thisGroup).get(i);
                TypedValue otherResult = groupToTypedValue.get(otherGroup).get(i);
                int cmp = thisResult.compareTo(otherResult);
                if (cmp == 0) continue;
                return orderByList.get(i).getValue() ? -cmp : cmp;
            }
            return 0;
        });

        groups.clear();
        groups.addAll(Arrays.asList(groupArr));
    }

    /**
     * 根据 table reference 生成一张虚表。
     */
    private Table makeReferenceTable(TableReference tableReference) throws SQLExecuteException {
        if (tableReference == null) return Table.dummyTable();

        if (tableReference instanceof TableJoin) {
            TableJoin tableJoin = (TableJoin) tableReference;
            return doJoin(tableJoin);
        } else if (tableReference instanceof RealTableFactor) {
            RealTableFactor realTableFactor = (RealTableFactor) tableReference;
            String tableName = realTableFactor.getTableName();
            Table table = TableManager.getInstance().getTable(tableName);
            if (table == null) {
                throw new SQLExecuteException("cannot find table %s", tableName);
            }
            String alias = realTableFactor.getTableNameAlias();
            if (alias == null) alias = tableName;

            // 此时需要复制表，因为我们会修改别名
            List<Column> columnList = table.getColumns();
            List<Column> newColumnList = new ArrayList<>();
            for (int i = 0; i < columnList.size(); i++) {
                Column column = columnList.get(i);
                Column newColumn = new Column(i, column.getColumnName(), column.getDataType(), alias);
                newColumnList.add(newColumn);
            }
            Table newTable = new Table(tableName, newColumnList);
            // 不能是 getRows，否则执行 where 子句进行过滤时会把原表的数据给删了
            newTable.addRows(table.getRows());
            return newTable;
        } else { // derived table
            DerivedTable derivedTable = (DerivedTable) tableReference;
            return new SelectExecutor(context).doSelect(derivedTable.getSelectStatement(), derivedTable.getAlias());
        }
    }

    private Table doJoin(TableJoin tableJoin) throws SQLExecuteException {
        Table left = makeReferenceTable(tableJoin.getLeft());
        Table right = makeReferenceTable(tableJoin.getRight());

        // 先合并列信息
        List<Column> leftColumnList = left.getColumns();
        List<Column> rightColumnList = right.getColumns();
        List<Column> newColumnList = new ArrayList<>();
        for (int i = 0; i < leftColumnList.size(); i++) {
            Column column = leftColumnList.get(i);
            Column newColumn = new Column(i, column.getColumnName(),
                    column.getDataType(), column.getTableName());
            newColumnList.add(newColumn);
        }
        for (int i = 0; i < rightColumnList.size(); i++) {
            Column column = rightColumnList.get(i);
            Column newColumn = new Column(leftColumnList.size() + i,
                    column.getColumnName(), column.getDataType(), column.getTableName());
            newColumnList.add(newColumn);
        }

        Table newTable = new Table(tableJoin.toString(), newColumnList);

        // 对 rows 进行 join
        Schema schema = newTable.getSchema();
        Expression condition = tableJoin.getOn() == null ? new IntExpression(1) : tableJoin.getOn();
        List<Row> leftRows = left.getRows();
        List<Row> rightRows = right.getRows();
        boolean[] needLeftJoin = new boolean[leftRows.size()];
        for (int i = 0; i < leftRows.size(); i++) {
            needLeftJoin[i] = true;
            Row leftRow = leftRows.get(i);
            for (Row rightRow : rightRows) {
                List<Cell> newCells = new ArrayList<>();
                newCells.addAll(leftRow.getCells());
                newCells.addAll(rightRow.getCells());
                Row newRow = new Row(newCells);
                if (predicate(context, schema, newRow, condition)) {
                    newTable.addRow(newRow);
                    needLeftJoin[i] = false;
                }
            }
        }

        // 如果是左连接的话，还需要把左边的表并进去
        if (tableJoin.getJoinType() == TableJoin.JoinType.LEFT) {
            for (int i = 0; i < leftRows.size(); i++) {
                if (needLeftJoin[i]) {
                    List<Cell> newCells = new ArrayList<>(leftRows.get(i).getCells());
                    for (int j = 0; j < right.getColumnCount(); j++) {
                        newCells.add(new Cell(TypedValue.NULL));
                    }
                    newTable.addRow(new Row(newCells));
                }
            }
        }

        return newTable;
    }

    private static class Group {

        private final List<TypedValue> items;

        /**
         * items 中的 Object 是 immutable 的。
         */
        public Group(@NotNull List<TypedValue> items) {
            this.items = items;
        }

        public List<TypedValue> getItems() {
            return items;
        }

        /**
         * 当且仅当两个 group 内部的列表元素完全相等的时候，两个 group 相等。
         */
        @Override
        public boolean equals(Object other) {
            if (other == this) return true;
            if (!(other instanceof Group)) return false;
            Group otherGroup = (Group) other;
            List<TypedValue> otherItems = otherGroup.getItems();
            if (items.size() != otherItems.size()) return false;
            for (int i = 0; i < items.size(); i++) {
                if (!items.get(i).equals(otherItems.get(i))) return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(items.toArray());
        }
    }
}
