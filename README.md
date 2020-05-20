# how-sql-works

SQL 的解析与执行。

## TODO

### 将只有一行一列的表看作一个值

比如支持

```sql
select (select max(grade) from student) as highest_grade;
```

### 支持正则表达式



