package com.github.afkbrb.sql;

import com.github.afkbrb.sql.ast.expressions.*;
import com.github.afkbrb.sql.ast.statements.*;
import com.github.afkbrb.sql.ast.statements.SelectStatement.*;
import com.github.afkbrb.sql.model.DataType;
import com.github.afkbrb.sql.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.afkbrb.sql.TokenType.*;

/**
 * 递归下降分析器。
 */
public class Parser {

    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    //****************************************** start *******************************************

    /**
     * <pre>
     * statementList
     *     : (statement SEMICOLON)* EOF // 没有把 ; 算在 statement 中是为了便于处理子查询
     * ;
     * </pre>
     */
    public List<Statement> statementList() throws SQLParseException {
        List<Statement> statementList = new ArrayList<>();
        while (lexer.currentType() != EOF) {
            Statement statement = statement();
            statementList.add(statement);
            match(SEMICOLON);
        }
        match(EOF);
        return statementList;
    }

    /**
     * <pre>
     * statement
     *     : createStatement
     *     | dropStatement
     *     | insertStatement
     *     | updateStatement
     *     | deleteStatement
     *     | selectStatement
     * ;
     * </pre>
     * <p>
     * 注意，此处不包含 ;
     */
    public Statement statement() throws SQLParseException {
        Token token = lexer.current();
        switch (token.getType()) {
            case CREATE:
                return createStatement();
            case DROP:
                return dropStatement();
            case INSERT:
                return insertStatement();
            case SELECT:
                return selectStatement();
            case UPDATE:
                return updateStatement();
            case DELETE:
                return deleteStatement();
            default:
                throw new SQLParseException("expected CREATE/DROP/INSERT/SELECT/UPDATE/DELETE, but got " + token);
        }
    }

    //****************************************** start *******************************************

    //****************************************** statement *******************************************

    /**
     * <pre>
     * createTableStatement
     *     : CREATE TABLE tableName OPEN_PAR columnName columnType (COMMA columnName columnType)* CLOSE_PAR
     * ;
     * </pre>
     */
    public CreateStatement createStatement() throws SQLParseException {
        match(CREATE);
        match(TABLE);
        String tableName = match(IDENTIFIER).getText();
        List<CreateStatement.ColumnDefinition> columnDefinitionList = new ArrayList<>();
        match(OPEN_PAR);
        String columnName = match(IDENTIFIER).getText();
        DataType dataType = tokenTypeToDataType(matchAny(INT, DOUBLE, STRING).getType());
        CreateStatement.ColumnDefinition columnDefinition = new CreateStatement.ColumnDefinition(columnName, dataType);
        columnDefinitionList.add(columnDefinition);
        while (lexer.currentType() == COMMA) {
            match(COMMA);
            columnName = match(IDENTIFIER).getText();
            dataType = tokenTypeToDataType(matchAny(INT, DOUBLE, STRING).getType());
            columnDefinition = new CreateStatement.ColumnDefinition(columnName, dataType);
            columnDefinitionList.add(columnDefinition);
        }
        match(CLOSE_PAR);

        return new CreateStatement(tableName, columnDefinitionList);
    }

    /**
     * <pre>
     * dropTableStatement
     *     : DROP TABLE tableName
     * ;
     * </pre>
     */
    public DropStatement dropStatement() throws SQLParseException {
        match(DROP);
        match(TABLE);
        String tableName = match(IDENTIFIER).getText();
        return new DropStatement(tableName);
    }

    /**
     * <pre>
     * insertStatement
     *     : INSERT INTO tableName (OPEN_PAR columnName (COMMA columnName)* CLOSE_PAR)? \
     *     VALUES OPEN_PAR expr (COMMA expr)* CLOSE_PAR
     * ;
     * </pre>
     */
    public InsertStatement insertStatement() throws SQLParseException {
        match(INSERT);
        match(INTO);
        String tableName = match(IDENTIFIER).getText();
        List<String> columnList = new ArrayList<>();
        List<Expression> valueList = new ArrayList<>();
        if (lexer.currentType() == OPEN_PAR) {
            match(OPEN_PAR);
            String columnName = match(IDENTIFIER).getText();
            columnList.add(columnName);
            while (lexer.currentType() == COMMA) {
                match(COMMA);
                columnName = match(IDENTIFIER).getText();
                columnList.add(columnName);
            }
            match(CLOSE_PAR);
        }
        match(VALUES);
        match(OPEN_PAR);
        valueList.add(expression());
        while (lexer.currentType() == COMMA) {
            match(COMMA);
            valueList.add(expression());
        }
        match(CLOSE_PAR);

        return new InsertStatement(tableName, columnList, valueList);
    }

    /**
     * <pre>
     * updateStatement
     *     : UPDATE tableName SET columnName EQ expr (COMMA columnName EQ expr)* (WHERE expr)?
     * ;
     * </pre>
     */
    public UpdateStatement updateStatement() throws SQLParseException {
        match(UPDATE);
        String tableName = match(IDENTIFIER).getText();
        match(SET);
        List<Pair<String, Expression>> updateList = new ArrayList<>();
        String columnName = match(IDENTIFIER).getText();
        match(EQ);
        Expression newValue = expression();
        updateList.add(new Pair<>(columnName, newValue));
        while (lexer.currentType() == COMMA) {
            match(COMMA);
            columnName = match(IDENTIFIER).getText();
            match(EQ);
            newValue = expression();
            updateList.add(new Pair<>(columnName, newValue));
        }

        Expression whereCondition = null;
        if (lexer.currentType() == WHERE) {
            match(WHERE);
            whereCondition = expression();
        }

        return new UpdateStatement(tableName, updateList, whereCondition);
    }

    /**
     * <pre>
     * deleteStatement
     *     : DELETE FROM tableName (WHERE expr)?
     * ;
     * </pre>
     */
    public DeleteStatement deleteStatement() throws SQLParseException {
        match(DELETE);
        match(FROM);
        String tableName = match(IDENTIFIER).getText();
        Expression expression = null;
        if (lexer.currentType() == WHERE) {
            match(WHERE);
            expression = expression();
        }
        return new DeleteStatement(tableName, expression);
    }

    /**
     * <pre>
     * selectStatement
     *     : SELECT expr (AS? alias)? (COMMA expr (AS? alias)?)* \
     *     (FROM tableReference (WHERE expr)? (GROUP BY expr (COMMA expr)* (HAVING expr)?)? \
     *     (ORDER BY expr (ASC | DESC)? (COMMA expr (ASC | DESC)?)*)? (LIMIT expr (OFFSET expr)?)?)?
     *     // 最后一个 )? 指 FROM 及后面的从句都可以省略
     * ;
     * </pre>
     */
    public SelectStatement selectStatement() throws SQLParseException {
        match(SELECT);

        List<Pair<Expression, String>> selectItemList = new ArrayList<>();
        selectItemList.add(new Pair<>(expression(), alias()));
        while (lexer.currentType() == COMMA) {
            match(COMMA);
            selectItemList.add(new Pair<>(expression(), alias()));
        }

        if (lexer.currentType() != FROM) return new SelectStatement(selectItemList,
                null, null, null, null, null);

        match(FROM);
        TableReference tableReference = tableReference();

        Expression whereCondition = null;
        if (lexer.currentType() == WHERE) {
            match(WHERE);
            whereCondition = expression();
        }

        GroupBy groupBy = null;
        if (lexer.currentType() == GROUP) {
            match(GROUP);
            match(BY);
            List<Expression> groupByList = new ArrayList<>();
            groupByList.add(expression());
            while (lexer.currentType() == COMMA) {
                match(COMMA);
                groupByList.add(expression());
            }

            Expression havingCondition = null;
            if (lexer.currentType() == HAVING) {
                match(HAVING);
                havingCondition = expression();
            }

            groupBy = new GroupBy(groupByList, havingCondition);
        }

        OrderBy orderBy = null;
        if (lexer.currentType() == ORDER) {
            match(ORDER);
            match(BY);
            List<Pair<Expression, Boolean>> orderByList = new ArrayList<>();
            Expression expression = expression();
            boolean desc = false;
            if (lexer.currentType() == ASC || lexer.currentType() == DESC) {
                desc = matchAny(ASC, DESC).getType() == DESC;
            }
            orderByList.add(new Pair<>(expression, desc));
            while (lexer.currentType() == COMMA) {
                match(COMMA);
                expression = expression();
                desc = false;
                if (lexer.currentType() == ASC || lexer.currentType() == DESC) {
                    desc = matchAny(ASC, DESC).getType() == DESC;
                }
                orderByList.add(new Pair<>(expression, desc));
            }

            orderBy = new OrderBy(orderByList);
        }

        Limit limit = null;
        if (lexer.currentType() == LIMIT) {
            match(LIMIT);
            Expression limitExpression = expression();
            Expression offsetExpression = null;
            if (lexer.currentType() == OFFSET) {
                match(OFFSET);
                offsetExpression = expression();
            }
            limit = new Limit(limitExpression, offsetExpression);
        }

        return new SelectStatement(selectItemList, tableReference,
                whereCondition, groupBy, orderBy, limit);
    }

    //****************************************** statement *******************************************

    //****************************************** fragment *******************************************

    /**
     * 如果没有 alias 的话就返回 null
     */
    private String alias() throws SQLParseException {
        if (lexer.currentType() == AS) {
            match(AS);
        }
        if (lexer.currentType() == IDENTIFIER) {
            return match(IDENTIFIER).getText();
        }

        return null;
    }

    /**
     * <pre>
     * tableReference
     *     : tableFactor ((INNER | LEFT) JOIN tableFactor (ON expr)?)*
     * ;
     * </pre>
     */
    private TableReference tableReference() throws SQLParseException {
        TableReference left = tableFactor();
        while (lexer.currentType() == INNER || lexer.currentType() == LEFT) {
            TableJoin.JoinType joinType = matchAny(INNER, LEFT).getType() == INNER ? TableJoin.JoinType.INNER : TableJoin.JoinType.LEFT;
            match(JOIN);
            TableReference right = tableFactor();
            Expression on = null;
            if (lexer.currentType() == ON) {
                match(ON);
                on = expression();
            }
            left = new TableJoin(joinType, left, right, on);
        }
        return left;
    }

    /**
     * <pre>
     * tableFactor
     *     : tableName (AS? alias)?
     *     | OPEN_PAR selectStatement CLOSE_PAR AS? alias // 要求派生表拥有别名
     *     | OPEN_PAR tableReference CLOSE_PAR
     * ;
     * </pre>
     */
    private TableReference tableFactor() throws SQLParseException {
        if (lexer.currentType() == IDENTIFIER) {
            String tableName = match(IDENTIFIER).getText();
            String alias = alias();
            return new RealTableFactor(tableName, alias);
        } else {
            match(OPEN_PAR);
            if (lexer.currentType() == SELECT) {
                SelectStatement selectStatement = selectStatement();
                match(CLOSE_PAR);
                String alias = alias();
                if (alias == null) throw new SQLParseException("Every derived table must have its own alias");
                return new DerivedTable(selectStatement, alias);
            } else {
                TableReference tableReference = tableReference();
                match(CLOSE_PAR);
                return tableReference;
            }
        }
    }

    //****************************************** fragment *******************************************

    //****************************************** expression 解析 *******************************************
    // 使用 Pratt 解析法处理优先级可能会更优美些，但此处还是选择分优先级递归下降

    /**
     * <pre>
     * expr
     *     : andExpr (OR andExpr)*
     * ;
     * </pre>
     */
    public Expression expression() throws SQLParseException {
        Expression left = andExpression();
        while (lexer.currentType() == OR) {
            match(OR);
            Expression right = andExpression();
            left = new OrExpression(left, right);
        }
        return left;
    }

    /**
     * <pre>
     * andExpr
     *     : notExpr (AND notExpr)*
     * ;
     * </pre>
     */
    private Expression andExpression() throws SQLParseException {
        Expression left = notExpression();
        while (lexer.currentType() == AND) {
            match(AND);
            Expression right = notExpression();
            left = new AndExpression(left, right);
        }
        return left;
    }

    /**
     * <pre>
     * notExpr
     *     : NOT notExpr // 支持 not 套娃
     *     | betweenExpr
     * ;
     * </pre>
     */
    private Expression notExpression() throws SQLParseException {
        if (lexer.currentType() == NOT) {
            match(NOT);
            return new NotExpression(notExpression());
        }
        return betweenExpression();
    }

    /**
     * <pre>
     * betweenExpr
     *     : predicate BETWEEN betweenExpr AND betweenExpr // 支持 between 套娃
     *     | predicate
     * ;
     * </pre>
     */
    private Expression betweenExpression() throws SQLParseException {
        Expression target = predicate();
        if (lexer.currentType() == BETWEEN) {
            match(BETWEEN);
            Expression left = betweenExpression();
            match(AND);
            Expression right = betweenExpression();
            return new BetweenExpression(target, left, right);
        }
        return target;
    }

    /**
     * <pre>
     * predicate
     *     : addExpr (NOT? IN OPEN_PAR (expr (COMMA expr)* | selectStatement) CLOSE_PAR
     *              | NOT? LIKE addExpr
     *              | NOT? REGEXP addExpr
     *              | IS NOT? NULL
     *              | (EQ | LT | GT | LE | GE | NE) addExpr)*
     * ;
     * </pre>
     */
    private Expression predicate() throws SQLParseException {
        Expression left = addExpression();
        TokenType nextType = lexer.currentType();
        while (nextType == NOT || nextType == IN || nextType == LIKE || nextType == REGEXP || nextType == IS
                || nextType == EQ || nextType == LT || nextType == GT || nextType == LE || nextType == GE || nextType == NE) {
            if (nextType == EQ || nextType == LT || nextType == GT ||
                    nextType == LE || nextType == GE || nextType == NE) {
                matchAny(EQ, LT, GT, LE, GE, NE);
                Expression right = addExpression();
                left = new BinaryExpression(tokenTypeToBinaryOperatorType(nextType), left, right);
            } else if (nextType == IS) {
                match(IS);
                boolean not = false;
                if (lexer.currentType() == NOT) {
                    not = true;
                    match(NOT);
                }
                match(NULL);
                left = new IsNullExpression(not, left);
            } else {
                boolean not = false;
                if (nextType == NOT) {
                    match(NOT);
                    not = true;
                }
                if ((lexer.currentType() == LIKE)) {
                    match(LIKE);
                    left = new LikeExpression(not, left, addExpression());
                } else if (lexer.currentType() == REGEXP) {
                    match(REGEXP);
                    left = new RegexpExpression(not, left, addExpression());
                } else {
                    match(IN);
                    match(OPEN_PAR);
                    if (lexer.currentType() == SELECT) {
                        SelectStatement subQuery = selectStatement();
                        left = new InSubQueryExpression(not, left, subQuery);
                    } else {
                        List<Expression> list = new ArrayList<>();
                        list.add(expression());
                        while (lexer.currentType() == COMMA) {
                            match(COMMA);
                            list.add(expression());
                        }
                        left = new InListExpression(not, left, list);
                    }
                    match(CLOSE_PAR);
                }
            }

            nextType = lexer.currentType();
        }

        return left;
    }

    /**
     * <pre>
     * addExpr
     *     : multExpr ((ADD | MINUS) multExpr)*
     * ;
     * </pre>
     */
    private Expression addExpression() throws SQLParseException {
        Expression left = multExpr();
        while (lexer.currentType() == ADD || lexer.currentType() == MINUS) {
            BinaryExpression.BinaryOperatorType op = tokenTypeToBinaryOperatorType(matchAny(ADD, MINUS).getType());
            Expression right = multExpr();
            left = new BinaryExpression(op, left, right);
        }
        return left;
    }

    /**
     * <pre>
     * multExpr
     *     : unaryExpr ((MULT | DIV) unaryExpr)*
     * ;
     * </pre>
     */
    private Expression multExpr() throws SQLParseException {
        Expression left = unaryExpr();
        while (lexer.currentType() == MULT || lexer.currentType() == DIV) {
            BinaryExpression.BinaryOperatorType op = tokenTypeToBinaryOperatorType(matchAny(MULT, DIV).getType());
            Expression right = unaryExpr();
            left = new BinaryExpression(op, left, right);
        }
        return left;
    }

    /**
     * <pre>
     * unaryExpr
     *     : (ADD | MINUS) unaryExpr
     *     | atomExpr
     *     ;
     * </pre>
     */
    private Expression unaryExpr() throws SQLParseException {
        if (lexer.currentType() == ADD || lexer.currentType() == MINUS) {
            UnaryExpression.UnaryOperationType op = tokenTypeToUnaryOperatorType(matchAny(ADD, MINUS).getType());
            return new UnaryExpression(op, unaryExpr());
        }
        return atomExpression();
    }

    /**
     * <pre>
     * atomExpr
     *     : INT_LITERAL
     *     | DOUBLE_LITERAL
     *     | STRING_LITERAL
     *     | NULL
     *     | MULT // *
     *     | IDENTIFIER DOT MULT // tableName.*
     *     | IDENTIFIER DOT IDENTIFIER // tableName.columnName
     *     | IDENTIFIER // columnName
     *     | IDENTIFIER OPEN_PAR (expr (COMMA expr)*)? CLOSE_PAR // 函数调用
     *     | EXISTS? selectStatement // 子查询
     *     | OPEN_PAR expr CLOSE_PAR
     * ;
     * </pre>
     */
    private Expression atomExpression() throws SQLParseException {
        switch (lexer.currentType()) {
            case INT_LITERAL:
                String intLiteral = match(INT_LITERAL).getText();
                return new IntExpression(Integer.parseInt(intLiteral));
            case DOUBLE_LITERAL:
                String doubleLiteral = match(DOUBLE_LITERAL).getText();
                return new DoubleExpression(Double.parseDouble(doubleLiteral));
            case STRING_LITERAL:
                return new StringExpression(match(STRING_LITERAL).getText());
            case NULL:
                match(NULL);
                return new NullExpression();
            case MULT:
                match(MULT);
                return new WildcardExpression();
            case IDENTIFIER:
                String identifier = match(IDENTIFIER).getText();
                if (lexer.currentType() == DOT) {
                    match(DOT);
                    if (lexer.currentType() == MULT) {
                        match(MULT);
                        return new WildcardExpression(identifier);
                    } else if (lexer.currentType() == IDENTIFIER) {
                        String columnName = match(IDENTIFIER).getText();
                        return new ColumnNameExpression(identifier, columnName);
                    } else {
                        throw new SQLParseException("Expected */identifier");
                    }
                } else if (lexer.currentType() == OPEN_PAR) {
                    match(OPEN_PAR);
                    List<Expression> argumentList = new ArrayList<>();
                    if (lexer.currentType() == CLOSE_PAR) { // 没有参数
                        match(CLOSE_PAR);
                        return new FunctionCallExpression(identifier, argumentList);
                    } else {
                        argumentList.add(expression());
                        while (lexer.currentType() == COMMA) {
                            match(COMMA);
                            argumentList.add(expression());
                        }
                        match(CLOSE_PAR);
                        return new FunctionCallExpression(identifier, argumentList);
                    }
                } else {
                    return new ColumnNameExpression(identifier);
                }
            case EXISTS:
                match(EXISTS);
                match(OPEN_PAR);
                SelectStatement subQuery = selectStatement();
                match(CLOSE_PAR);
                return new SubQueryExpression(true, subQuery);
            case OPEN_PAR:
                match(OPEN_PAR);
                if (lexer.currentType() == SELECT) {
                    subQuery = selectStatement();
                    match(CLOSE_PAR);
                    return new SubQueryExpression(false, subQuery);
                } else {
                    Expression expression = expression();
                    match(CLOSE_PAR);
                    return expression;
                }
            default:
                throw new SQLParseException("expected an atom expression, but got " + lexer.current());
        }
    }

    //****************************************** expression 解析 *******************************************

    //****************************************** util *******************************************

    /**
     * 获取当前 token，并断言其类型为 tokenType，然后移动到下一个 token。
     */
    private Token match(TokenType tokenType) throws SQLParseException {
        return matchAny(tokenType);
    }

    /**
     * 任意匹配一个。
     */
    private Token matchAny(TokenType... tokenTypes) throws SQLParseException {
        Token current = lexer.consume();
        for (TokenType tokenType : tokenTypes) {
            if (current.getType() == tokenType) {
                return current;
            }
        }
        throw new SQLParseException("expected " + Arrays.toString(tokenTypes) + " but got " + current);
    }

    private static DataType tokenTypeToDataType(TokenType tokenType) throws SQLParseException {
        switch (tokenType) {
            case INT:
                return DataType.INT;
            case DOUBLE:
                return DataType.DOUBLE;
            case STRING:
                return DataType.STRING;
            default:
                throw new SQLParseException("expected INT/DOUBLE/TEXT, but got " + tokenType);
        }
    }

    private static BinaryExpression.BinaryOperatorType tokenTypeToBinaryOperatorType(TokenType tokenType) throws SQLParseException {
        switch (tokenType) {
            case ADD:
                return BinaryExpression.BinaryOperatorType.ADD;
            case MINUS:
                return BinaryExpression.BinaryOperatorType.MINUS;
            case MULT:
                return BinaryExpression.BinaryOperatorType.MULT;
            case DIV:
                return BinaryExpression.BinaryOperatorType.DIV;
            case EQ:
                return BinaryExpression.BinaryOperatorType.EQ;
            case LT:
                return BinaryExpression.BinaryOperatorType.LT;
            case GT:
                return BinaryExpression.BinaryOperatorType.GT;
            case LE:
                return BinaryExpression.BinaryOperatorType.LE;
            case GE:
                return BinaryExpression.BinaryOperatorType.GE;
            case NE:
                return BinaryExpression.BinaryOperatorType.NE;
            default:
                throw new SQLParseException("expected a binary operator type, but got " + tokenType);
        }
    }

    private static UnaryExpression.UnaryOperationType tokenTypeToUnaryOperatorType(TokenType tokenType) throws SQLParseException {
        switch (tokenType) {
            case ADD:
                return UnaryExpression.UnaryOperationType.ADD;
            case MINUS:
                return UnaryExpression.UnaryOperationType.MINUS;
            default:
                throw new SQLParseException("expected a unary operator type, but got " + tokenType);
        }
    }

    //****************************************** util *******************************************
}
