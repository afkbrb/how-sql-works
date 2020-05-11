package com.github.afkbrb.sql;

import com.github.afkbrb.sql.ast.expressions.*;
import com.github.afkbrb.sql.ast.statements.*;
import com.github.afkbrb.sql.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.afkbrb.sql.TokenType.*;

/**
 * 无脑递归下降就完事了。
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
    public List<Statement> statementList() throws ParseException {
        List<Statement> statementList = new ArrayList<>();
        while (lexer.current().getType() != EOF) {
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
     *     : createTableStatement
     *     | dropTableStatement
     *     | insertStatement
     *     | selectStatement
     *     | updateStatement
     *     | deleteStatement
     * ;
     * </pre>
     */
    private Statement statement() throws ParseException {
        Token token = lexer.current();
        switch (token.getType()) {
            case CREATE:
                return createTableStatement();
            case DROP:
                return dropTableStatement();
            case INSERT:
                return insertStatement();
            case SELECT:
                return selectStatement();
            case UPDATE:
                return updateStatement();
            case DELETE:
                return deleteStatement();
            default:
                throw new ParseException("expect CREATE/DROP/INSERT/SELECT/UPDATE/DELETE, but get " + token);
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
    private CreateStatement createTableStatement() throws ParseException {
        match(CREATE);
        match(TABLE);
        String tableName = match(IDENTIFIER).getText();
        List<CreateStatement.ColumnDefinition> columnDefinitionList = new ArrayList<>();
        match(OPEN_PAR);
        String columnName = match(IDENTIFIER).getText();
        CreateStatement.DataType dataType = tokenTypeToDataType(matchAny(INT, DOUBLE, TEXT).getType());
        CreateStatement.ColumnDefinition columnDefinition = new CreateStatement.ColumnDefinition(columnName, dataType);
        columnDefinitionList.add(columnDefinition);
        while (lexer.current().getType() == COMMA) {
            match(COMMA);
            columnName = match(IDENTIFIER).getText();
            dataType = tokenTypeToDataType(matchAny(INT, DOUBLE, TEXT).getType());
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
    private DropStatement dropTableStatement() throws ParseException {
        match(DROP);
        match(TABLE);
        String tableName = match(IDENTIFIER).getText();
        return new DropStatement(tableName);
    }

    /**
     * <pre>
     * insertStatement
     *     : INSERT INTO tableName (OPEN_PAR columnName (COMMA columnName)* CLOSE_PAR)? VALUES OPEN_PAR expr (COMMA expr)* CLOSE_PAR
     * ;
     * </pre>
     */
    private InsertStatement insertStatement() throws ParseException {
        match(INSERT);
        match(INTO);
        String tableName = match(IDENTIFIER).getText();
        List<String> columnList = new ArrayList<>();
        List<Expression> valueList = new ArrayList<>();
        if (lexer.current().getType() == OPEN_PAR) {
            match(OPEN_PAR);
            String columnName = match(IDENTIFIER).getText();
            columnList.add(columnName);
            while (lexer.current().getType() == COMMA) {
                match(COMMA);
                columnName = match(IDENTIFIER).getText();
                columnList.add(columnName);
            }
            match(CLOSE_PAR);
        }
        match(VALUES);
        match(OPEN_PAR);
        valueList.add(expression());
        while (lexer.current().getType() == COMMA) {
            match(COMMA);
            valueList.add(expression());
        }
        match(CLOSE_PAR);

        return new InsertStatement(tableName, columnList, valueList);
    }

    /**
     * <pre>
     * selectStatement
     *     : SELECT (ALL | DISTINCT)? expr (AS? alias)? (COMMA expr (AS? alias)?)* \
     *     (FROM tableReference)? (WHERE expr)? (GROUP BY expr)? (HAVING expr)? \
     *     (ORDER BY expr (ASC | DESC)?)? (LIMIT expr (OFFSET expr)?)?
     * ;
     * </pre>
     */
    private SelectStatement selectStatement() throws ParseException {
        match(SELECT);

        SelectStatement.SelectOption selectOption = null;
        if (lexer.current().getType() == ALL || lexer.current().getType() == DISTINCT) {
            selectOption = tokenTypeToSelectOption(matchAny(ALL, DISTINCT).getType());
        }

        List<Pair<Expression, String>> selectItemList = new ArrayList<>();
        selectItemList.add(new Pair<>(expression(), alias()));
        while (lexer.current().getType() == COMMA) {
            match(COMMA);
            selectItemList.add(new Pair<>(expression(), alias()));
        }

        SelectStatement.TableReference tableReference = null;
        if (lexer.current().getType() == FROM) {
            match(FROM);
            tableReference = tableReference();
        }

        Expression whereCondition = null;
        if (lexer.current().getType() == WHERE) {
            match(WHERE);
            whereCondition = expression();
        }

        Expression groupByExpression = null;
        if (lexer.current().getType() == GROUP) {
            match(GROUP);
            match(BY);
            groupByExpression = expression();
        }

        Expression havingCondition = null;
        if (lexer.current().getType() == HAVING) {
            match(HAVING);
            havingCondition = expression();
        }

        SelectStatement.OrderBy orderBy = null;
        if (lexer.current().getType() == ORDER) {
            match(ORDER);
            match(BY);
            Expression orderByExpression = expression();
            boolean desc = false;
            if (lexer.current().getType() == ASC || lexer.current().getType() == DESC) {
                desc = matchAny(ASC, DESC).getType() == DESC;
            }
            orderBy = new SelectStatement.OrderBy(orderByExpression, desc);
        }

        SelectStatement.Limit limit = null;
        if (lexer.current().getType() == LIMIT) {
            match(LIMIT);
            Expression limitExpression = expression();
            Expression offsetExpression = null;
            if (lexer.current().getType() == OFFSET) {
                match(OFFSET);
                offsetExpression = expression();
            }
            limit = new SelectStatement.Limit(limitExpression, offsetExpression);
        }

        return new SelectStatement(selectOption, selectItemList, tableReference,
                whereCondition, groupByExpression, havingCondition, orderBy, limit);
    }

    /**
     * <pre>
     * updateStatement
     *     : UPDATE tableName SET columnName EQ expr (COMMA columnName EQ expr)* (WHERE expr)?
     * ;
     * </pre>
     */
    private UpdateStatement updateStatement() throws ParseException {
        match(UPDATE);
        String tableName = match(IDENTIFIER).getText();
        match(SET);
        List<Pair<String, Expression>> updateList = new ArrayList<>();
        String columnName = match(IDENTIFIER).getText();
        match(EQ);
        Expression newValue = expression();
        updateList.add(new Pair<>(columnName, newValue));
        while (lexer.current().getType() == COMMA) {
            match(COMMA);
            columnName = match(IDENTIFIER).getText();
            match(EQ);
            newValue = expression();
            updateList.add(new Pair<>(columnName, newValue));
        }

        Expression whereCondition = null;
        if (lexer.current().getType() == WHERE) {
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
    private DeleteStatement deleteStatement() throws ParseException {
        match(DELETE);
        match(FROM);
        String tableName = match(IDENTIFIER).getText();
        Expression expression = null;
        if (lexer.current().getType() == WHERE) {
            match(WHERE);
            expression = expression();
            return new DeleteStatement(tableName, expression);
        }
        return new DeleteStatement(tableName, expression);
    }

    //****************************************** statement *******************************************

    //****************************************** fragment *******************************************

    /**
     * 如果没有 alias 的话就返回 null
     */
    private String alias() throws ParseException {
        if (lexer.current().getType() == AS) {
            match(AS);
        }
        if (lexer.current().getType() == IDENTIFIER) {
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
    private SelectStatement.TableReference tableReference() throws ParseException {
        SelectStatement.TableReference left = tableFactor();
        while (lexer.current().getType() == INNER || lexer.current().getType() == LEFT) {
            SelectStatement.TableJoin.JoinType joinType = matchAny(INNER, LEFT).getType() == INNER ? SelectStatement.TableJoin.JoinType.INNER : SelectStatement.TableJoin.JoinType.LEFT;
            match(JOIN);
            SelectStatement.TableReference right = tableFactor();
            Expression on = null;
            if (lexer.current().getType() == ON) {
                match(ON);
                on = expression();
            }
            left = new SelectStatement.TableJoin(joinType, left, right, on);
        }
        return left;
    }

    /**
     * <pre>
     * tableFactor
     *     : tableName (AS? tableNameAlias)?
     *     | OPEN_PAR (selectStatement | tableReference) CLOSE_PAR (AS? tableNameAlias)?
     * ;
     * </pre>
     */
    private SelectStatement.TableReference tableFactor() throws ParseException {
        if (lexer.current().getType() == IDENTIFIER) {
            String tableName = match(IDENTIFIER).getText();
            String alias = alias();
            return new SelectStatement.RealTableFactor(tableName, alias);
        } else {
            match(OPEN_PAR);
            if (lexer.current().getType() == SELECT) {
                SelectStatement selectStatement = selectStatement();
                match(CLOSE_PAR);
                String alias = alias();
                return new SelectStatement.SubQueryFactor(selectStatement, alias);
            } else {
                SelectStatement.TableReference tableReference = tableReference();
                match(CLOSE_PAR);
                String alias = alias();
                return new SelectStatement.TableReferenceFactor(tableReference, alias);
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
    private Expression expression() throws ParseException {
        Expression left = andExpression();
        while (lexer.current().getType() == OR) {
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
    private Expression andExpression() throws ParseException {
        Expression left = notExpression();
        while (lexer.current().getType() == AND) {
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
    private Expression notExpression() throws ParseException {
        if (lexer.current().getType() == NOT) {
            match(NOT);
            return new NotExpression(notExpression());
        }
        return betweenExpression();
    }

    /**
     * <pre>
     * betweenExpr
     *     : BETWEEN betweenExpr AND betweenExpr // 支持 between 套娃
     *     | predict
     * ;
     * </pre>
     */
    private Expression betweenExpression() throws ParseException {
        Expression target = predict();
        if (lexer.current().getType() == BETWEEN) {
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
     * predict
     *     : addExpr (NOT? IN OPEN_PAR (expr (COMMA expr)* | selectStatement) CLOSE_PAR
     *              | NOT? LIKE addExpr
     *              | IS NOT? NULL
     *              | (EQ | LT | GT | LE | GE | NE) addExpr)*
     * ;
     * </pre>
     */
    private Expression predict() throws ParseException {
        Expression left = addExpression();
        TokenType nextType = lexer.current().getType();
        while (nextType == NOT || nextType == IN || nextType == LIKE || nextType == IS || nextType == EQ
                || nextType == LT || nextType == GT || nextType == LE || nextType == GE || nextType == NE) {
            if (nextType == EQ || nextType == LT || nextType == GT ||
                    nextType == LE || nextType == GE || nextType == NE) {
                matchAny(EQ, LT, GT, LE, GE, NE);
                Expression right = addExpression();
                left = new BinaryExpression(tokenTypeToBinaryOperatorType(nextType), left, right);
            } else if (nextType == IS) {
                match(IS);
                boolean not = false;
                if (lexer.current().getType() == NOT) {
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
                if ((lexer.current().getType() == LIKE)) {
                    match(LIKE);
                    Expression right = addExpression();
                    left = new LikeExpression(not, left, right);
                } else {
                    match(IN);
                    match(OPEN_PAR);
                    if (lexer.current().getType() == SELECT) {
                        SelectStatement right = selectStatement();
                        left = new InSubQueryExpression(not, left, right);
                    } else {
                        List<Expression> rightList = new ArrayList<>();
                        rightList.add(expression());
                        while (lexer.current().getType() == COMMA) {
                            match(COMMA);
                            rightList.add(expression());
                        }
                        left = new InListExpression(not, left, rightList);
                    }
                    match(CLOSE_PAR);
                }
            }

            nextType = lexer.current().getType();
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
    private Expression addExpression() throws ParseException {
        Expression left = multExpr();
        while (lexer.current().getType() == ADD || lexer.current().getType() == MINUS) {
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
    private Expression multExpr() throws ParseException {
        Expression left = unaryExpr();
        while (lexer.current().getType() == MULT || lexer.current().getType() == DIV) {
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
    private Expression unaryExpr() throws ParseException {
        if (lexer.current().getType() == ADD || lexer.current().getType() == MINUS) {
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
     *     | TEXT_LITERAL
     *     | MULT // *
     *     | IDENTIFIER // 包括 foo.bar 形式
     *     | IDENTIFIER DOT MULT // foo.*
     *     | IDENTIFIER OPEN_PAR (expr (COMMA expr)*)? CLOSE_PAR // 函数调用
     *     | OPEN_PAR expr CLOSE_PAR
     * ;
     * </pre>
     */
    private Expression atomExpression() throws ParseException {
        switch (lexer.current().getType()) {
            case INT_LITERAL:
                String intLiteral = match(INT_LITERAL).getText();
                return new IntExpression(Integer.parseInt(intLiteral));
            case DOUBLE_LITERAL:
                String doubleLiteral = match(DOUBLE_LITERAL).getText();
                return new DoubleExpression(Double.parseDouble(doubleLiteral));
            case TEXT_LITERAL:
                return new TextExpression(match(TEXT_LITERAL).getText());
            case MULT:
                match(MULT);
                return new IdentifierExpression("*");
            case IDENTIFIER:
                String identifier = match(IDENTIFIER).getText();
                if (lexer.current().getType() == OPEN_PAR) {
                    match(OPEN_PAR);
                    List<Expression> argumentList = new ArrayList<>();
                    if (lexer.current().getType() == CLOSE_PAR) { // 没有参数
                        match(CLOSE_PAR);
                        return new FunctionCallExpression(identifier, argumentList);
                    } else {
                        argumentList.add(expression());
                        while (lexer.current().getType() == COMMA) {
                            match(COMMA);
                            argumentList.add(expression());
                        }
                        match(CLOSE_PAR);
                        return new FunctionCallExpression(identifier, argumentList);
                    }
                } else {
                    return new IdentifierExpression(identifier);
                }
            case OPEN_PAR:
                match(OPEN_PAR);
                Expression expression = expression();
                match(CLOSE_PAR);
                return expression;
            default:
                throw new ParseException("expect an atom expression, but get " + lexer.current());
        }
    }

    //****************************************** expression 解析 *******************************************

    //****************************************** util *******************************************

    /**
     * 获取当前 token，并断言其类型为 tokenType，然后移动到下一个 token。
     */
    private Token match(TokenType tokenType) throws ParseException {
        return matchAny(tokenType);
    }

    /**
     * 任意匹配一个。
     */
    private Token matchAny(TokenType... tokenTypes) throws ParseException {
        Token current = lexer.consume();
        for (TokenType tokenType : tokenTypes) {
            if (current.getType() == tokenType) {
                return current;
            }
        }
        throw new ParseException("expect " + Arrays.toString(tokenTypes) + " but get " + current);
    }

    private static CreateStatement.DataType tokenTypeToDataType(TokenType tokenType) throws ParseException {
        switch (tokenType) {
            case INT:
                return CreateStatement.DataType.INT;
            case DOUBLE:
                return CreateStatement.DataType.DOUBLE;
            case TEXT:
                return CreateStatement.DataType.TEXT;
            default:
                throw new ParseException("expect INT/DOUBLE/TEXT, but get " + tokenType);
        }
    }

    private static SelectStatement.SelectOption tokenTypeToSelectOption(TokenType tokenType) throws ParseException {
        switch (tokenType) {
            case ALL:
                return SelectStatement.SelectOption.ALL;
            case DISTINCT:
                return SelectStatement.SelectOption.DISTINCT;
            default:
                throw new ParseException("expect ALL/DISTINCT, but get " + tokenType);
        }
    }

    private static BinaryExpression.BinaryOperatorType tokenTypeToBinaryOperatorType(TokenType tokenType) throws ParseException {
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
                throw new ParseException("expect a binary operator type, but get " + tokenType);
        }
    }

    private static UnaryExpression.UnaryOperationType tokenTypeToUnaryOperatorType(TokenType tokenType) throws ParseException {
        switch (tokenType) {
            case ADD:
                return UnaryExpression.UnaryOperationType.ADD;
            case MINUS:
                return UnaryExpression.UnaryOperationType.MINUS;
            default:
                throw new ParseException("expect a unary operator type, but get " + tokenType);
        }
    }

    //****************************************** util *******************************************
}
