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
    public Token lookAhead(int distance) throws ParseException {
        assert distance >= 0;
        while (distance >= lookAheadBuffer.size()) {
            lookAheadBuffer.add(next());
        }

        Token token = lookAheadBuffer.get(distance);
        return new Token(token.getType(), token.getText()); // copy
    }

    public Token current() throws ParseException {
        return lookAhead(0);
    }

    public Token peek() throws ParseException {
        return lookAhead(1);
    }

    /**
     * “吃掉”当前 token，移动到下一个 token。
     */
    public Token consume() throws ParseException {
        lookAhead(0); // 先保证 buffer 中有 token
        return lookAheadBuffer.remove(0);
    }

    private Token next() throws ParseException {
        int ch;
        ch = nextChar();
        while (isBlank(ch)) { // 忽略空白符
            ch = nextChar();
        }
        if (ch == -1) {
            return new Token(EOF, "");
        }
        switch (ch) {
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
                    error("expect '=' after '!'");
                } else {
                    return new Token(NE, "!=");
                }
            case '\'':
            case '"':
                rollback();
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

    private Token getTextLiteral() throws ParseException {
        int quote = nextChar();
        assert quote == '\'' || quote == '"';
        StringBuilder sb = new StringBuilder();

        int ch = nextChar();
        while (ch != -1) {
            if (ch == quote) return new Token(TEXT_LITERAL, sb.toString());

            if (ch == '\\') { // 处理对引号的转义
                int t = nextChar();
                if (t == -1) {
                    error("unexpected EOF");
                } else if (t == quote) {
                    sb.append((char) quote);
                } else {
                    sb.append('\\');
                    sb.append((char) t);
                }
            } else {
                sb.append((char) ch);
            }

            ch = nextChar();
        }
        error("unexpected EOF");

        return new Token(EOF, "");
    }

    private Token getIntOrDoubleLiteral() throws ParseException {
        StringBuilder sb = new StringBuilder();
        boolean isDouble = false;
        int ch = nextChar();
        while (isDigit(ch)) {
            sb.append((char) ch);
            ch = nextChar();
        }
        if (ch == '.') {
            isDouble = true;
            sb.append((char) ch);
            ch = nextChar();

            if (!isDigit(ch)) {
                error("expect a digit after '.'");
            }

            while (isDigit(ch)) {
                sb.append((char) ch);
                ch = nextChar();
            }
        }

        rollback(); // 读取了一个未使用的字符

        return new Token(isDouble ? DOUBLE_LITERAL : INT_LITERAL, sb.toString());
    }

    private Token getIdentifierOrKeyword() throws ParseException {
        StringBuilder sb = new StringBuilder();

        sb.append(doGetIdentifier());
        while (nextChar() == '.') { // 支持形如 foo.bar 这样的 identifier
            sb.append(".");
            if (nextChar() == '*') { // foo.*
                sb.append("*");
                return new Token(IDENTIFIER, sb.toString()); // 直接返回，必不是 keyword
            }
            rollback(); // 读取了一个非 * 字符
            sb.append(doGetIdentifier());
        }
        rollback(); // 读取了一个非 . 字符

        String t = sb.toString().toUpperCase();
        if (keywords.containsKey(t)) {
            return new Token(keywords.get(t), t);
        } else {
            return new Token(IDENTIFIER, sb.toString());
        }
    }

    private String doGetIdentifier() throws ParseException {
        StringBuilder sb = new StringBuilder();

        int ch = nextChar();
        if (!(isLetter(ch) || ch == '_')) {
            error("expect [a-zA-Z_]");
        }
        sb.append((char) ch);

        ch = nextChar();
        while (isDigit(ch) || isLetter(ch) || ch == '_') {
            sb.append((char) ch);
            ch = nextChar();
        }

        rollback(); // 读取了一个未使用的字符

        return sb.toString();
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

    private void error(String msg) throws ParseException {
        throw new ParseException(msg);
    }

    private static boolean isBlank(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
    }

    private static boolean isDigit(int ch) {
        return '0' <= ch && ch <= '9';
    }

    private static boolean isLetter(int ch) {
        return ('a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z');
    }
}
