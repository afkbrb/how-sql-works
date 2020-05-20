# how-sql-works

SQL 的解析与执行。

## TODO

### 将只有一行一列的表看作一个值

比如支持

```sql
select (select max(grade) from student) as highest_grade;
```

### 符号表支持

目前没有符号表，内部表达式没法引用外部的列。

符号表需要包括 schema 和 row，可以将 schema 和 row 组合成一个 context。context 遇到 join 就横向拓展，遇到子查询就纵向拓展（将子查询的 context 附加到 schema 头部）。

设计一个 InheritedContext，保存父类的 context 信息，这样子查询就可以访问父类列的数据了。

### 聚集函数的嵌套可以在对单行进行 evaluate 时检测出来

### 将 executors 改成动态的，主要是考虑到 SelectExecutor 中的 InheritedContext

### 支持正则表达式



