grammar SQL;

statementList
    : (statement SEMICOLON)* EOF // 没有把 ; 算在 statement 中是为了便于处理子查询
;

statement
    : createTableStatement
    | dropTableStatement
    | insertStatement
    | selectStatement
    | updateStatement
    | deleteStatement
;

createTableStatement
    : CREATE TABLE tableName OPEN_PAR columnName columnType (COMMA columnName columnType)* CLOSE_PAR
;

dropTableStatement
    : DROP TABLE tableName
;

insertStatement
    : INSERT INTO tableName (OPEN_PAR columnName (COMMA columnName)* CLOSE_PAR)?
    VALUES OPEN_PAR expr (COMMA expr)* CLOSE_PAR
;

selectStatement
    : SELECT expr (AS? alias)? (COMMA expr (AS? alias)?)*
    (FROM tableReference (WHERE expr)? (GROUP BY expr (COMMA expr)* (HAVING expr)?)?
    (ORDER BY expr (ASC | DESC)? (COMMA expr (ASC | DESC)?)*)? (LIMIT expr (OFFSET expr)?)?)?
    // 最后一个 )? 指 FROM 及后面的从句都可以省略
;

updateStatement
    : UPDATE tableName SET columnName EQ expr (COMMA columnName EQ expr)* (WHERE expr)?
;

deleteStatement
    : DELETE FROM tableName (WHERE expr)?
;

tableReference
    : tableFactor ((INNER | LEFT) JOIN tableFactor (ON expr)?)*
;

tableFactor
    : tableName (AS? alias)?
    | OPEN_PAR selectStatement CLOSE_PAR AS? alias // 要求派生表用于别名
    | OPEN_PAR tableReference CLOSE_PAR
;

expr
    : andExpr (OR andExpr)*
;

andExpr
    : notExpr (AND notExpr)*
;

notExpr
    : NOT notExpr // 支持 not 套娃
    | betweenExpr
;

betweenExpr
    : predict BETWEEN betweenExpr AND betweenExpr // 支持 between 套娃
    | predict
;

predict
    : addExpr (NOT? IN OPEN_PAR (expr (COMMA expr)* | selectStatement) CLOSE_PAR
             | NOT? LIKE addExpr
             | NOT? REGEXP addExpr
             | IS NOT? NULL
             | (EQ | LT | GT | LE | GE | NE) addExpr)*
;

addExpr
    : multExpr ((ADD | MINUS) multExpr)*
;

multExpr
    : unaryExpr ((MULT | DIV) unaryExpr)*
;

unaryExpr
    : (ADD | MINUS) unaryExpr
    | atomExpr
    ;

atomExpr
    : INT_LITERAL
    | DOUBLE_LITERAL
    | STRING_LITERAL
    | NULL
    | MULT // *
    | IDENTIFIER DOT MULT // tableName.*
    | IDENTIFIER DOT IDENTIFIER // tableName.columnName
    | IDENTIFIER // columnName
    | IDENTIFIER OPEN_PAR (expr (COMMA expr)*)? CLOSE_PAR // 函数调用
    | EXISTS? selectStatement // 子查询
    | OPEN_PAR expr CLOSE_PAR
;

columnType
    : INT
    | DOUBLE
    | STRING
    ;

tableName
    : IDENTIFIER
    ;

columnName
    : IDENTIFIER
    ;

alias
    : IDENTIFIER
    ;

EQ: '=';
LT: '<';
GT: '>';
LE: '<=';
GE: '>=';
NE: '!=';

ADD: '+';
MINUS: '-';
MULT: '*';
DIV: '/';

COMMA: ',';
DOT: '.';
SEMICOLON: ';';
OPEN_PAR: '(';
CLOSE_PAR: ')';

AND: A N D;
AS: A S;
ASC: A S C;
BETWEEN: B E T W E E N;
BY: B Y;
CREATE: C R E A T E;
DELETE: D E L E T E;
DESC: D E S C;
DOUBLE: D O U B L E;
DROP: D R O P;
EXISTS: E X I S T S;
FALSE: F A L S E;
FROM: F R O M;
GROUP: G R O U P;
HAVING: H A V I N G;
IN: I N;
INNER: I N N E R;
INSERT: I N S E R T;
INT: I N T;
INTO: I N T O;
IS: I S;
JOIN: J O I N;
LEFT: L E F T;
LIKE: L I K E;
LIMIT: L I M I T;
NOT: N O T;
NULL: N U L L;
OFFSET: O F F S E T;
ON: O N;
OR: O R;
ORDER: O R D E R;
OUTER: O U T E R;
REGEXP: R E G E X P;
RIGHT: R I G H T;
SELECT: S E L E C T;
SET: S E T;
STRING: S T R I N G;
TABLE: T A B L E;
TRUE: T R U E;
UPDATE: U P D A T E;
VALUES: V A L U E S;
WHERE: W H E R E;

INT_LITERAL: DIGIT+;

DOUBLE_LITERAL: DIGIT+ DOT DIGIT+;
 // 字符串字面量用单引号包围，包含单引号和反斜杠的话要转义
STRING_LITERAL: '\'' ( ~['\\] | '\\\'' | '\\\\' )* '\'';

IDENTIFIER: [a-zA-Z_] [a-zA-Z_0-9]*;

SINGLE_LINE_COMMENT: '--' ~[\r\n]* -> channel(HIDDEN);

MULTILINE_COMMENT: '/*' .*? ( '*/' | EOF ) -> channel(HIDDEN);

SPACES: [ \u000B\t\r\n] -> channel(HIDDEN);

UNEXPECTED_CHAR: .;

fragment DIGIT : [0-9];

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];

