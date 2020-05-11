package com.github.afkbrb.sql.ast;

import com.github.afkbrb.sql.ASTVisitor;

public interface ASTNode {
    void accept(ASTVisitor visitor);
}
