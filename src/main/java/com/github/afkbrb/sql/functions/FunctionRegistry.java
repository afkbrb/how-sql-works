package com.github.afkbrb.sql.functions;

import java.util.HashMap;
import java.util.Map;

public class FunctionRegistry {

    public static final Map<String, Function> nameToFunction = new HashMap<>();

    static {
        registerFunction(new Max());
        registerFunction(new Count());
    }

    private static void registerFunction(Function function) {
        nameToFunction.putIfAbsent(function.getName().toLowerCase(), function);
    }

    /**
     * 根据函数名获取函数，忽略大小写。
     */
    public static Function getFunction(String functionName) {
        return nameToFunction.get(functionName.toLowerCase());
    }
}
