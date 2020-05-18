package com.github.afkbrb.sql.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Cell {

    private TypedValue typedValue;

    public Cell(@NotNull TypedValue typedValue) {
        this.typedValue = Objects.requireNonNull(typedValue);
    }

    @NotNull
    public TypedValue getTypedValue() {
        return typedValue;
    }

    public void setTypedValue(@NotNull TypedValue typedValue) {
        this.typedValue = typedValue;
    }

    @Override
    public String toString() {
        return typedValue.toString();
    }
}
