package com.github.afkbrb.sql;

import com.github.afkbrb.sql.ast.statements.*;
import com.github.afkbrb.sql.executors.*;
import com.github.afkbrb.sql.model.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

public class Shell {

    public static void main(String[] args) {
        System.out.println("Welcome :)");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("SQL> ");
                String statements = reader.readLine();
                if (statements.equals("quit")) {
                    System.out.println("Bye :)");
                    break;
                }
                Lexer lexer = new Lexer(new StringReader(statements));
                Parser parser = new Parser(lexer);
                List<Statement> statementList;
                statementList = parser.statementList();
                for (Statement statement : statementList) {
                    if (statement instanceof SelectStatement) {
                        Table table = new SelectExecutor().doSelect((SelectStatement) statement);
                        System.out.println(prettify(table.toString()));
                    } else if (statement instanceof CreateStatement) {
                        CreateExecutor.doCreate((CreateStatement) statement);
                    } else if (statement instanceof DropStatement) {
                        DropExecutor.doDrop((DropStatement) statement);
                    } else if (statement instanceof InsertStatement) {
                        InsertExecutor.doInsert((InsertStatement) statement);
                    } else if (statement instanceof UpdateStatement) {
                        UpdateExecutor.doUpdate((UpdateStatement) statement);
                    } else {
                        DeleteExecutor.doDelete((DeleteStatement) statement);
                    }
                }
            } catch (SQLParseException e) {
                System.out.println("syntax error: " + e.getMessage());
            } catch (SQLExecuteException e) {
                System.out.println("execute error: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("unexpected I/O exception: " + e.getMessage());
            }
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String prettify(String jsonString) {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        JsonElement el = JsonParser.parseString(jsonString);
        return gson.toJson(el);
    }
}
