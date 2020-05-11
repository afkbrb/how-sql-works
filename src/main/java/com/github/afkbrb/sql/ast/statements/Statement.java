package com.github.afkbrb.sql.ast.statements;

import com.github.afkbrb.sql.ast.ASTNode;

import java.util.Collections;
import java.util.List;

public abstract class Statement implements ASTNode {

    protected <T> List<T> ensureNonNull(List<T> list) {
        if (list != null) return list;
        return Collections.emptyList();
    }
}
