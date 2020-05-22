package com.github.afkbrb.sql;

import com.github.afkbrb.sql.ast.statements.*;
import com.github.afkbrb.sql.executors.*;
import com.github.afkbrb.sql.model.Column;
import com.github.afkbrb.sql.model.Row;
import com.github.afkbrb.sql.model.Table;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.List;

import static com.github.afkbrb.sql.utils.StringWidth.stringWidth;

public class Shell {

    private Mode mode = Mode.COLUMN;
    private boolean debug = false;

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        new Shell().serve(args);
    }

    public void serve(String[] args) {
        welcome();
        while (true) {
            try {
                String command = readCommand();
                // .quit 和 .exit 比较特殊，在此处理
                if (command.equals(".quit") || command.equalsIgnoreCase(".exit")) {
                    System.out.println("Bye :)");
                    break;
                }
                if (handleMetaCommand(command)) continue;

                Lexer lexer = new Lexer(new StringReader(command));
                Parser parser = new Parser(lexer);
                Statement statement = parser.statementList().get(0);

                if (debug) System.out.println(statement);

                if (statement instanceof SelectStatement) {
                    Table table = new SelectExecutor().doSelect((SelectStatement) statement);
                    if (mode == Mode.COLUMN) {
                        printColumn(table);
                    } else {
                        printJson(table);
                    }
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

            } catch (SQLParseException e) {
                System.out.println("Syntax error: " + e.getMessage());
            } catch (SQLExecuteException e) {
                System.out.println("Execute error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected exception: " + e.getMessage());
                e.printStackTrace();
            }
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从控制读取命令，允许多行输入。
     * <p>
     * TODO: 用于可能输入奇怪的字符，需要处理转义
     */
    private String readCommand() throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean quote = false;
        char last = ' ';
        System.out.print("SQL> ");
        String line = reader.readLine();
        if (line.trim().startsWith(".")) return line; // meta command
        while (true) {
            for (char current : line.toCharArray()) {
                sb.append(current);
                if (current == '\'' && last != '\\') {
                    quote = !quote;
                } else if (current == ';' && !quote) {
                    return sb.toString();
                }
                last = current;
            }
            sb.append(' '); // 将换行处理成空格
            System.out.print("  -> ");
            line = reader.readLine();
        }
    }

    /**
     * 仿 sqlite 元命令。
     */
    private boolean handleMetaCommand(String command) throws IOException, SQLParseException, SQLExecuteException {
        String[] split = command.trim().toLowerCase().split("\\s+");
        if (split.length == 0) return true; // 什么都不做
        if (!split[0].startsWith(".")) return false;
        switch (split[0]) {
            case ".help":
                help();
                break;
            case ".schema":
                if (split.length < 2) {
                    System.out.println("Usage: .schema <table name>");
                } else {
                    schema(split[1]);
                }
                break;
            case ".tables":
                tables();
                break;
            case ".mode":
                if (split.length < 2 || !(split[1].equals("column") || split[1].equals("json"))) {
                    System.out.println("Usage: .mode <column | json>");
                } else {
                    mode = split[1].equals("column") ? Mode.COLUMN : Mode.JSON;
                }
                break;
            case ".debug":
                if (split.length < 2 || !(split[1].equals("on") || split[1].equals("off"))) {
                    System.out.println("Usage: .debug <on | off>");
                } else {
                    debug = split[1].equals("on");
                }
                break;
            case ".source":
                if (split.length < 2) {
                    System.out.println("Usage: .source <filename>");
                } else {
                    source(split[1]);
                }
                break;
            default:
                System.out.println("Unknown meta command '" + split[0] + "', enter '.help' for usage hints");
                break;
        }
        return true;
    }

    private void welcome() {
        System.out.println("Welcome :)");
        System.out.println("Enter '.help' for usage hints");
    }

    private void help() {
        System.out.printf("%-24s Change debug mode, ast will be echoed if set to on\n", ".debug <on | off>");
        System.out.printf("%-24s Exit this program\n", ".exit");
        System.out.printf("%-24s Show this message\n", ".help");
        System.out.printf("%-24s Set output mode of select statements\n", ".mode <column | json>");
        System.out.printf("%-24s Exit this program\n", ".quit");
        System.out.printf("%-24s Show the description of a table\n", ".schema <table name>");
        System.out.printf("%-24s Execute SQL statements from a file\n", ".source <filename>");
        System.out.printf("%-24s Show all tables\n", ".tables");
    }

    private void schema(String tableName) {
        Table table = TableManager.getInstance().getTable(tableName);
        if (table == null) {
            System.out.println("Table \"" + tableName + "\" not found");
        } else {
            List<Column> columns = table.getSchema().getColumns();
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            boolean first = true;
            for (Column column : columns) {
                if (!first) sb.append(", ");
                sb.append(column.getColumnName());
                sb.append(" ");
                sb.append(column.getDataType().name());
                first = false;
            }
            sb.append(")");
            System.out.println(sb);
        }
    }

    private void tables() {
        List<Table> tables = TableManager.getInstance().getTables();
        for (Table table : tables) {
            System.out.print(table.getTableName() + " ");
        }
        System.out.println();
    }

    private void source(String filename) throws IOException, SQLParseException, SQLExecuteException {
        // 去掉引号
        if (filename.length() > 2 && (filename.charAt(0) == '"' && filename.charAt(filename.length() - 1) == '"')
                || (filename.charAt(0) == '\'' && filename.charAt(filename.length() - 1) == '\'')) {
            filename = filename.substring(1, filename.length() - 1);
        }

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("file '" + filename + "' doesn't exist");
            return;
        }
        if (file.isDirectory()) {
            System.out.println("'" + filename + "' is a directory");
            return;
        }
        if (!file.canRead()) {
            System.out.println("cannot read file '" + filename + "'");
            return;
        }

        System.out.println("Executing SQL statements from " + file.getCanonicalPath());
        Reader reader = new BufferedReader(new FileReader(file));
        Parser parser = new Parser(new Lexer(reader));
        try {
            List<Statement> statementList = parser.statementList();
            for (Statement statement : statementList) {
                // 此处不执行 select
                if (statement instanceof CreateStatement) {
                    CreateExecutor.doCreate((CreateStatement) statement);
                } else if (statement instanceof DropStatement) {
                    DropExecutor.doDrop((DropStatement) statement);
                } else if (statement instanceof InsertStatement) {
                    InsertExecutor.doInsert((InsertStatement) statement);
                } else if (statement instanceof UpdateStatement) {
                    UpdateExecutor.doUpdate((UpdateStatement) statement);
                } else if (statement instanceof DeleteStatement) {
                    DeleteExecutor.doDelete((DeleteStatement) statement);
                }
            }
            System.out.println("Total SQL statements executed: " + statementList.size());
        } finally {
            reader.close();
        }
    }

    @SuppressWarnings("all")
    private void printColumn(Table table) {
        if (table.getColumnCount() == 0 || table.getRowCount() == 0) return;
        int[] width = new int[table.getColumnCount()];
        for (int i = 0; i < width.length; i++) {
            width[i] = stringWidth(table.getColumns().get(i).getColumnName().toString());
        }
        for (int i = 0; i < width.length; i++) {
            for (Row row : table.getRows()) {
                width[i] = Math.max(width[i], stringWidth(row.getCell(i).getTypedValue().getValue().toString()));
            }
        }

        for (int i = 0; i < width.length; i++) {
            width[i] += 2;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("+");
        for (int i = 0; i < width.length; i++) {
            for (int j = 0; j < width[i]; j++) {
                sb.append("-");
            }
            sb.append("+");
        }
        sb.append("\n");

        sb.append("|");
        for (int i = 0; i < width.length; i++) {
            sb.append(" ");
            String columnName = table.getColumns().get(i).getColumnName();
            sb.append(columnName);
            int w = stringWidth(columnName);
            for (int j = 0; j < width[i] - w - 1; j++) {
                sb.append(" ");
            }
            sb.append("|");
        }
        sb.append("\n");

        sb.append("+");
        for (int i = 0; i < width.length; i++) {
            for (int j = 0; j < width[i]; j++) {
                sb.append("-");
            }
            sb.append("+");
        }
        sb.append("\n");

        for (int k = 0; k < table.getRowCount(); k++) {
            sb.append("|");
            for (int i = 0; i < width.length; i++) {
                sb.append(" ");
                String value = table.getRow(k).getCell(i).getTypedValue().getValue().toString();
                sb.append(value);
                int w = stringWidth(value);
                for (int j = 0; j < width[i] - w - 1; j++) {
                    sb.append(" ");
                }
                sb.append("|");
            }
            sb.append("\n");
        }

        sb.append("+");
        for (int i = 0; i < width.length; i++) {
            for (int j = 0; j < width[i]; j++) {
                sb.append("-");
            }
            sb.append("+");
        }
        sb.append("\n");

        System.out.println(sb);
    }

    private void printJson(Table table) {
        System.out.println(prettify(table.toString()));
    }

    private static String prettify(String jsonString) {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        JsonElement el = JsonParser.parseString(jsonString);
        return gson.toJson(el);
    }

    private enum Mode {
        COLUMN,
        LIST,
        JSON
    }
}
