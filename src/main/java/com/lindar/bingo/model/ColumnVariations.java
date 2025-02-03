package com.lindar.bingo.model;

import java.util.List;
import java.util.Map;

public class ColumnVariations {

    // Maximum Numbers and Variations for First Column
    public static final int NUMBERS_FIRST_COLUMN = 9;
    public static final Map<Integer, List<Integer>> VARIATIONS_FIRST_COLUMN = Map.of(
            0, List.of(1, 1, 1, 2, 2, 2),
            1, List.of(1, 1, 1, 1, 2, 3)
    );

    // Maximum Numbers and Variations for Middle Column(s)
    public static final int NUMBERS_MIDDLE_COLUMN = 10;
    public static final Map<Integer, List<Integer>> VARIATIONS_MIDDLE_COLUMNS = Map.of(
            0, List.of(1, 1, 1, 1, 3, 3),
            1, List.of(1, 1, 1, 2, 2, 3),
            2, List.of(1, 1, 2, 2, 2, 2)
    );

    // Maximum Numbers and Variations for Last Column
    public static final int NUMBERS_LAST_COLUMN = 11;
    public static final Map<Integer, List<Integer>> VARIATIONS_LAST_COLUMN = Map.of(
            0,List.of(1, 1, 1, 2, 3, 3),
            1, List.of(1, 1, 2, 2, 2, 3),
            2, List.of(1, 2, 2, 2, 2, 2)
    );
}
