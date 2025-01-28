package com.lindar.bingo.model;

import java.util.ArrayList;
import java.util.List;

public enum ColumnRange {
    FIRST(1, 9),
    SECOND(10, 19),
    THIRD(20, 29),
    FOURTH(30, 39),
    FIFTH(40, 49),
    SIXTH(50, 59),
    SEVENTH(60, 69),
    EIGHTH(70, 79),
    NINTH(80, 90);

    private final int start;
    private final int end;

    ColumnRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public List<Integer> getNumbers() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            numbers.add(i);
        }
        return numbers;
    }
}
