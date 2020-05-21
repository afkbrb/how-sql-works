package com.github.afkbrb.sql.utils;

public final class JsonUtils {

    public static String jsonEscape(String jsonString) {
        jsonString = jsonString.replaceAll("\\\\", "\\\\\\\\"); // \ -> \\
        jsonString = jsonString.replaceAll("\"", "\\\\\""); // " -> \"
        return jsonString;
    }

    private JsonUtils() {}
}
