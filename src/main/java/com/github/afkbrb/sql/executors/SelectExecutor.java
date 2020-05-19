package com.github.afkbrb.sql.executors;

import com.github.afkbrb.sql.SQLExecuteException;
import com.github.afkbrb.sql.TableManager;
import com.github.afkbrb.sql.ast.expressions.Expression;
import com.github.afkbrb.sql.ast.expressions.IdentifierExpression;
import com.github.afkbrb.sql.ast.expressions.IntExpression;
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

    /**
     * 处理 select 比较麻烦，需要考虑多种情况。
     * <p>
     * TODO: distinct, all
     */
    public static Table doSelect(SelectStatement selectStatement) throws SQLExecuteException {
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
        List<Row> rows = referenceTable.getRows();

        if (selectStatement.getWhereCondition() != null) {
            for (Iterator<Row> iterator = rows.iterator(); iterator.hasNext(); ) {
                if (!predict(iterator.next(), schema, selectStatement.getWhereCondition())) iterator.remove();
            }
        }

        // 展开 *，顺便把别名处理了。
        List<Pair<Expression, String>> selectItemList = new ArrayList<>();
        for (Pair<Expression, String> pair : originalSelectItemList) {
            Expression expression = pair.getKey();
            if (expression instanceof IdentifierExpression && ((IdentifierExpression) expression).getIdentifier().contains("*")) {
                String identifier = ((IdentifierExpression) expression).getIdentifier();
                List<Column> columnList = schema.getColumnList();
                if (!identifier.equals("*")) {
                    // tableName.*
                    // TODO：返现 tableAlias 对应的 table 不存在的话应该报错
                    String[] split = identifier.split("\\.");
                    if (split.length != 2) error("invalid wildcard expression %s", identifier);
                    columnList = columnList.stream().filter(column -> column.getTableAlias().equalsIgnoreCase(split[0])).collect(Collectors.toList());
                }
                columnList.forEach(column -> selectItemList.add(new Pair<>(new IdentifierExpression(column.getTableAlias() + "." + column.getColumnName()), column.getColumnName())));
            } else {
                // 别名处理
                if (pair.getValue() == null) {
                    // 没有别名就自动生成别名
                    if (expression instanceof IdentifierExpression && ((IdentifierExpression) expression).getIdentifier().contains(".")) {
                        // 如果没有指定别名且列名为 tableName.columnName 的形式
                        String identifier = ((IdentifierExpression) expression).getIdentifier();
                        String[] split = identifier.split("\\.");
                        if (split.length != 2) error("invalid column name %s", identifier);
                        selectItemList.add(new Pair<>(expression, split[1]));
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
            String columnAlias = pair.getValue();
            DataType dataType = inferType(schema, pair.getKey());
            // 如果是 derived table 的话，这边的别名会被子其别名覆盖
            Column column = new Column(i, columnAlias, dataType, "result");
            columnList.add(column);
        }
        Table table = new Table("result", columnList);

        int limit = Integer.MAX_VALUE;
        int offset = 0;
        if (selectStatement.getLimit() != null) {
            TypedValue evaluatedLimit = evaluate(selectStatement.getLimit().getLimitExpression());
            if (!(evaluatedLimit.getValue() instanceof Integer)) {
                error("expected a integer result of evaluate");
            }
            limit = (int) evaluatedLimit.getValue();
            if (limit < 0) {
                error("expected limit >= 0");
            }
            if (selectStatement.getLimit().getOffsetExpression() != null) {
                TypedValue evaluatedOffset = evaluate(selectStatement.getLimit().getOffsetExpression());
                if (!(evaluatedOffset.getValue() instanceof Integer)) {
                    error("expected a integer result of evaluate");
                }
                offset = (int) evaluatedOffset.getValue();
                if (offset < 0) {
                    error("expected offset >= 0");
                }
            }
        }

        if (!isGroupBy && !isAggregate) {
            // 如果需要排序的话，先对 rows 进行排序
            if (selectStatement.getOrderBy() != null) {
                OrderBy orderBy = selectStatement.getOrderBy();
                List<Pair<Expression, Boolean>> orderByList = orderBy.getOrderByList();
                for (Pair<Expression, Boolean> pair : orderByList) {
                    sortRows(rows, schema, pair.getKey(), pair.getValue());
                }
            }

            // 填充数据
            int upperBound = Math.min(rows.size(), offset + limit);
            for (int i = offset; i < upperBound; i++) {
                Row row = rows.get(i);
                List<Cell> cells = new ArrayList<>();
                for (Expression expression : selectExpressionList) {
                    TypedValue value = evaluate(row, schema, expression);
                    cells.add(new Cell(value));
                }
                table.addRow(new Row(cells));
            }
        } else if (!isGroupBy) {
            // 使用了聚集函数的话，只有一行记录
            if (!(limit > 0 && offset == 0)) return table;
            List<Cell> cells = new ArrayList<>();
            for (Expression expression : selectExpressionList) {
                cells.add(new Cell(evaluate(rows, schema, expression)));
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
                    items.add(evaluate(row, schema, expression));
                }
                Group group = new Group(items);
                groupMap.putIfAbsent(group, new ArrayList<>());
                groupMap.get(group).add(row);
            }

            if (groupBy.getHavingCondition() != null) {
                for (Iterator<Map.Entry<Group, List<Row>>> iterator = groupMap.entrySet().iterator(); iterator.hasNext(); ) {
                    if (!predict(iterator.next().getValue(), schema, groupBy.getHavingCondition())) iterator.remove();
                }
            }

            ArrayList<Map.Entry<Group, List<Row>>> groupEntryList = new ArrayList<>(groupMap.entrySet());
            if (selectStatement.getOrderBy() != null) {
                OrderBy orderBy = selectStatement.getOrderBy();
                List<Pair<Expression, Boolean>> orderByList = orderBy.getOrderByList();
                for (Pair<Expression, Boolean> pair : orderByList) {
                    sortGroup(groupEntryList, schema, pair.getKey(), pair.getValue());
                }
            }

            int upperBound = Math.min(groupEntryList.size(), offset + limit);
            for (int i = offset; i < upperBound; i++) {
                List<Row> groupRows = groupEntryList.get(i).getValue();
                List<Cell> cells = new ArrayList<>();
                for (Expression expression : selectExpressionList) {
                    TypedValue value = evaluate(groupRows, schema, expression);
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
    private static boolean isAggregate(Expression expression) {
        AggregateDetector detector = new AggregateDetector(expression);
        return detector.detect();
    }

    /**
     * 推导 expression 的类型
     */
    private static DataType inferType(Schema schema, Expression expression) {
        TypeInferer inferer = new TypeInferer(schema);
        return inferer.infer(expression);
    }

    // TODO 处理 NULL
    private static void sortRows(List<Row> rows, Schema schema, Expression expression, boolean desc) throws SQLExecuteException {
        Double[] valueArr = new Double[rows.size()];
        Integer[] indexMap = new Integer[rows.size()]; // 用 int/double 无法使用 sort(T[] a, Comparator<? super T> c)
        for (int i = 0; i < valueArr.length; i++) {
            indexMap[i] = i;
            TypedValue typedValue = evaluate(rows.get(i), schema, expression);
            if (isNull(typedValue)) {
                valueArr[i] = 0.0; // 将 NULL 当 0 处理
            } else {
                valueArr[i] = ((Number) typedValue.getValue()).doubleValue();
            }
        }
        if (desc) {
            Arrays.sort(indexMap, (i, j) -> valueArr[j].compareTo(valueArr[i]));
        } else {
            Arrays.sort(indexMap, (i, j) -> valueArr[i].compareTo(valueArr[j]));
        }

        List<Row> tmp = new ArrayList<>(rows);
        for (int i = 0; i < rows.size(); i++) {
            rows.set(i, tmp.get(indexMap[i]));
        }
    }

    private static void sortGroup(List<Map.Entry<Group, List<Row>>> groupEntryList, Schema schema, Expression expression, boolean desc) throws SQLExecuteException {
        Double[] valueArr = new Double[groupEntryList.size()];
        Integer[] indexMap = new Integer[groupEntryList.size()]; // 用 int/double 无法使用 sort(T[] a, Comparator<? super T> c)
        for (int i = 0; i < valueArr.length; i++) {
            indexMap[i] = i;
            TypedValue typedValue = evaluate(groupEntryList.get(i).getValue(), schema, expression);
            if (isNull(typedValue)) {
                valueArr[i] = 0.0; // 将 NULL 当 0 处理
            } else {
                valueArr[i] = ((Number) typedValue.getValue()).doubleValue();
            }
        }
        if (desc) {
            Arrays.sort(indexMap, (i, j) -> valueArr[j].compareTo(valueArr[i]));
        } else {
            Arrays.sort(indexMap, (i, j) -> valueArr[i].compareTo(valueArr[j]));
        }

        ArrayList<Map.Entry<Group, List<Row>>> tmp = new ArrayList<>(groupEntryList);
        for (int i = 0; i < groupEntryList.size(); i++) {
            groupEntryList.set(i, tmp.get(indexMap[i]));
        }
    }

    /**
     * 根据 table reference 生成一张虚表。
     */
    private static Table makeReferenceTable(TableReference tableReference) throws SQLExecuteException {
        if (tableReference == null) return Table.DUMMY_TABLE;

        if (tableReference instanceof TableJoin) {
            TableJoin tableJoin = (TableJoin) tableReference;
            return doJoin(tableJoin);
        } else if (tableReference instanceof RealTableFactor) {
            RealTableFactor realTableFactor = (RealTableFactor) tableReference;
            String tableName = realTableFactor.getTableName();
            Table table = TableManager.getInstance().getTable(tableName);
            if (table == null) {
                error("cannot find table %s", tableName);
            }
            String alias = realTableFactor.getTableNameAlias();
            if (alias == null) alias = tableName;

            // 此时需要复制表（浅拷贝，rows 不变，主要是修改 column 信息），因为我们会修改别名
            List<Column> columnList = table.getColumns();
            List<Column> newColumnList = new ArrayList<>();
            for (int i = 0; i < columnList.size(); i++) {
                Column column = columnList.get(i);
                Column newColumn = new Column(i, column.getColumnName(), column.getDataType(), alias);
                newColumnList.add(newColumn);
            }
            Table newTable = new Table(tableName, newColumnList);
            // 不能是 getRows，否则执行 where 子句进行过滤时会把原表的数据给删了
            newTable.setRows(table.copyRows());
            return newTable;
        } else { // SubQueryFactor
            SubQueryFactor subQueryFactor = (SubQueryFactor) tableReference;
            Table table = doSelect(subQueryFactor.getSelectStatement());
            // 设置子查询别名
            table.getColumns().forEach(column -> column.setTableAlias(subQueryFactor.getAlias() == null ? "derived_table" : subQueryFactor.getAlias()));
            return table;
        }
    }

    private static Table doJoin(TableJoin tableJoin) throws SQLExecuteException {
        Table left = makeReferenceTable(tableJoin.getLeft());
        Table right = makeReferenceTable(tableJoin.getRight());

        // 先合并列信息
        List<Column> leftColumnList = left.getColumns();
        List<Column> rightColumnList = right.getColumns();
        List<Column> newColumnList = new ArrayList<>();
        for (int i = 0; i < leftColumnList.size(); i++) {
            Column column = leftColumnList.get(i);
            Column newColumn = new Column(i, column.getColumnName(),
                    column.getDataType(), column.getTableAlias());
            newColumnList.add(newColumn);
        }
        for (int i = 0; i < rightColumnList.size(); i++) {
            Column column = rightColumnList.get(i);
            Column newColumn = new Column(leftColumnList.size() + i,
                    column.getColumnName(), column.getDataType(), column.getTableAlias());
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
                if (predict(newRow, schema, condition)) {
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