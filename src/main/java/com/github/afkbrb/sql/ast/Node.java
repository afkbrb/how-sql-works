package com.github.afkbrb.sql.ast;

import com.github.afkbrb.sql.visitors.Visitor;

public interface Node {
    <T> T accept(Visitor<? extends T> visitor);
}
