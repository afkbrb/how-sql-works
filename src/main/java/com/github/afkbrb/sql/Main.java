package com.github.afkbrb.sql;

import com.github.afkbrb.sql.ast.statements.Statement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String statements = reader.readLine();
            if (statements.equals("quit")) break;
            Lexer lexer = new Lexer(new StringReader(statements));
            Parser parser = new Parser(lexer);
            List<Statement> statementList;
            try {
                statementList = parser.statementList();
                for (Statement statement : statementList) {
                    ASTStringViewer viewer = new ASTStringViewer(statement);
                    System.out.println(viewer.getASTString());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        reader.close();
    }
}
