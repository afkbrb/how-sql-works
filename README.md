# how-sql-works

SQL 的解析与执行，基于简易 CSV 数据库。

支持表的 create, delete, drop, insert, select, update 语句，select 语句支持子查询和内、左连接（右连接和外连接可通过左连接实现，索性就不直接支持了）。

没有提供对数据库的 CRUD 语句，一个目录就是一个数据库，一个 csv 文件就是一张表。

不支持事务、并发、视图、完整性约束等，一个玩具而已。

提供 SQLite 风格的元命令（meta command）。

```
afkbrb> java -jar how-sql-works.jar
Welcome :)
Enter '.help' for usage hints
Connected to a transient in-memory database
Use '.db <db directory>' to work on a persistent database
SQL> .help
.db <db directory>       Change database directory
.debug <on | off>        Change debug mode, ast will be echoed if set to on
.exit                    Exit this program
.help                    Show this message
.mode <column | json>    Set output mode of select statements
.quit                    Exit this program
.schema <table name>     Show the description of a table
.source <filename>       Execute SQL statements from a file
.tables                  Show all tables
SQL> .quit
Bye :)
```

## 架构

1. 使用一个递归下降分析器对 SQL 进行解析，生成抽象语法树。
2. 采用 visitor 模式遍历语法树，实现了类型推导器 ([TypeInferer](./src/main/java/com/github/afkbrb/sql/visitors/TypeInferer.java))，聚集函数检测器 ([AggregateDetector](./src/main/java/com/github/afkbrb/sql/visitors/AggregateDetector.java))，求值器 ([Evaluator](src/main/java/com/github/afkbrb/sql/visitors/AbstractEvaluator.java))，语法树打印器 ([ToStringVisitor](./src/main/java/com/github/afkbrb/sql/visitors/ToStringVisitor.java))。
3. 语法树被提交给执行器，执行器利用上面提到的类型推导器，聚集函数检测器（用于 select）和求值器完成对 SQL 语句的执行，执行结果通过 [TableManager](./src/main/java/com/github/afkbrb/sql/TableManager.java) 管理，如果指定了数据库目录的话，对数据的修改将被持久化。

## TODO

- [ ] 查询优化