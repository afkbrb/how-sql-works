package com.github.afkbrb.sql.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 代表一行记录，由一个 cell 列表组成，列名和列表 index 的对应关系由 table 指定。
 */
public class Row {

    private final List<Cell> cells;

    public static final Row EMPTY_ROW = new Row(Collections.emptyList());

    public Row(@NotNull List<Cell> cells) {
        this.cells = Objects.requireNonNull(cells);
    }

    public void setCell(int index, @NotNull TypedValue value) {
        if (index >= 0 && index < cells.size()) {
            Objects.requireNonNull(value);
            cells.get(index).setTypedValue(value);
            return;
        }
        throw new IndexOutOfBoundsException();
    }

    @Nullable
    public Cell getCell(int index) {
        if (index >= 0 && index < cells.size()) {
            return cells.get(index);
        }
        return null;
    }

    @NotNull
    public List<Cell> getCells() {
        return cells;
    }

    public int size() {
        return cells.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Cell cell : cells) {
            if (!first) sb.append(", ");
            sb.append(cell);
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
