/**
 * .source demo
 * 注意，.source 的输入文件中不能包含 .source, .mode, .help 这样的元命令，
 * 只能包含 create, insert, update, select, delete, drop 语句。
 * 执行时 select 语句将 *被忽略*。
 */

 -- 支持 '--' 注释，但不支持 '//' 注释（要支持也是十分简单的）
create table test1 (id int, name string, grade double);

insert into test1 values (1, '张三', 66.6);

insert into test1 values (2, 'afkbrb', 90.01);

insert into test1 values (3, 'squanchy', null);

-- source 时，select 将被忽略
select * from test1;

-- 😄 :)
