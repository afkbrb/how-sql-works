package com.github.afkbrb.sql;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import static com.github.afkbrb.sql.TokenType.*;

/**
 * 从流中获取 token。
 */
public class Lexer {

    private final Reader reader;
    private final List<Token> lookAheadBuffer = new LinkedList<>();

    public Lexer(Reader reader) {
        this.reader = reader;
    }

    /**
     * 向前看 distance 个 token，当 distance 为 0 时，表示当前 token。
     * <p>
     * 然而本项目的语法是 LL(0)，所以最终只用到了 lookAhead(0)。
     */
    public Token lookAhead(int distance) throws SQLParseException {
        assert distance >= 0;
        while (distance >= lookAheadBuffer.size()) {
            lookAheadBuffer.add(next());
        }

        Token token = lookAheadBuffer.get(distance);
        return new Token(token.getType(), token.getText()); // copy
    }

    public Token current() throws SQLParseException {
        return lookAhead(0);
    }

    public Token peek() throws SQLParseException {
        return lookAhead(1);
    }

    /**
     * “吃掉”当前 token，移动到下一个 token。
     */
    public Token consume() throws SQLParseException {
        lookAhead(0); // 先保证 buffer 中有 token
        return lookAheadBuffer.remove(0);
    }

    private Token next() throws SQLParseException {
        int next;
        next = nextChar();
        while (isBlank(next)) { // 忽略空白符
            next = nextChar();
        }
        if (next == -1) {
            return new Token(EOF, "");
        }
        switch (next) {
            case '+':
                return new Token(ADD, "+");
            case '-':
                return new Token(MINUS, "-");
            case '*':
                return new Token(MULT, "*");
            case '/':
                return new Token(DIV, "/");
            case ',':
                return new Token(COMMA, ",");
            case '.':
                return new Token(DOT, ".");
            case ';':
                return new Token(SEMICOLON, ";");
            case '(':
                return new Token(OPEN_PAR, "(");
            case ')':
                return new Token(CLOSE_PAR, ")");
            case '=':
                return new Token(EQ, "=");
            case '<':
                if (nextChar() == '=') {
                    return new Token(LE, "<=");
                } else {
                    rollback();
                    return new Token(LT, "<");
                }
            case '>':
                if (nextChar() == '=') {
                    return new Token(GE, ">=");
                } else {
                    rollback();
                    return new Token(GT, ">");
                }
            case '!': // 仅支持 !=
                if (nextChar() != '=') {
                    throw new SQLParseException("expect '=' after '!'");
                } else {
                    return new Token(NE, "!=");
                }
            case '\'':
                return getTextLiteral();
            case '0': // 竟然写出这种代码 🙂
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                rollback();
                return getIntOrDoubleLiteral();
            default:
                rollback();
                return getIdentifierOrKeyword();
        }
    }

    /**
     * 要求所有字符串由双引号围起来。
     */
    private Token getTextLiteral() throws SQLParseException {
        StringBuilder sb = new StringBuilder();

        int next = nextChar();
        while (next != -1) {
            if (next == '\'') return new Token(STRING_LITERAL, sb.toString());

            if (next == '\\') { // 处理对引号的转义
                int t = nextChar();
                if (t == -1) {
                    throw new SQLParseException("unexpected EOF");
                } else if (t == '\'') {
                    sb.append('\'');
                } else {
                    sb.append('\\');
                    sb.append((char) t);
                }
            } else {
                sb.append((char) next);
            }

            next = nextChar();
        }
        throw new SQLParseException("unexpected EOF");
    }

    private Token getIntOrDoubleLiteral() throws SQLParseException {
        StringBuilder sb = new StringBuilder();
        boolean isDouble = false;
        int next = nextChar();
        while (isDigit(next)) {
            sb.append((char) next);
            next = nextChar();
        }
        if (next == '.') {
            isDouble = true;
            sb.append((char) next);
            next = nextChar();

            if (!isDigit(next)) {
                throw new SQLParseException("expect a digit after '.'");
            }

            while (isDigit(next)) {
                sb.append((char) next);
                next = nextChar();
            }
        }

        rollback(); // 读取了一个未使用的字符

        return new Token(isDouble ? DOUBLE_LITERAL : INT_LITERAL, sb.toString());
    }

    private Token getIdentifierOrKeyword() throws SQLParseException {
        StringBuilder sb = new StringBuilder();

        int next = nextChar();
        if (!(isLetter(next) || next == '_')) {
            throw new SQLParseException("expected [a-zA-Z_], but got %c", (char) next);
        }
        sb.append((char) next);

        next = nextChar();
        while (isDigit(next) || isLetter(next) || next == '_') {
            sb.append((char) next);
            next = nextChar();
        }

        rollback(); // 读取了一个未使用的字符

        String t = sb.toString().toUpperCase();
        if (keywords.containsKey(t)) {
            return new Token(keywords.get(t), t);
        } else {
            return new Token(IDENTIFIER, sb.toString());
        }
    }

    private int nextChar() {
        assert reader.markSupported();
        try {
            reader.mark(1);
            return reader.read();
        } catch (IOException e) {
            System.err.println("unexpected IOException");
            e.printStackTrace();
        }

        return -1;
    }

    private void rollback() {
        assert reader.markSupported();
        try {
            reader.reset();
        } catch (IOException e) {
            System.out.println("unexpected IOException");
            e.printStackTrace();
        }
    }

    private static boolean isBlank(int next) {
        return next == ' ' || next == '\t' || next == '\r' || next == '\n';
    }

    private static boolean isDigit(int next) {
        return '0' <= next && next <= '9';
    }

    private static boolean isLetter(int next) {
        return ('a' <= next && next <= 'z' || 'A' <= next && next <= 'Z');
    }
}
