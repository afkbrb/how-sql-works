package com.github.afkbrb.sql.utils;

public final class JsonUtils {

    public static String jsonEscape(String jsonString) {
        jsonString = jsonString.replace("\\", "\\\\"); // \ -> \\
        jsonString = jsonString.replace("\"", "\\\""); // " -> \"
        return jsonString;
    }

    private JsonUtils() {}
}
