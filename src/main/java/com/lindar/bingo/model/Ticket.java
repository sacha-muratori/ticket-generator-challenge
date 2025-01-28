package com.lindar.bingo.model;

import java.util.Arrays;

public class Ticket {
    public static final int ROWS = 3; // Number of rows in a ticket
    public static final int COLUMNS = 9; // Number of columns in a ticket
    public static final int MAX_ROW_NUMBERS = 5; // Max Number of numbers in a row
    public static final int MAX_ROW_BLANK_SPACES = 4; // Max Number of blank spaces in a row
    public static final int MAX_COLUMN_BLANK_SPACES = 2; // Max Number of blank spaces in a column

    private final int[][] grid;

    public Ticket() {
        this.grid = new int[ROWS][COLUMNS]; // Initialize grid with zeros
    }

    public int[][] getGrid() {
        return grid;
    }

    public int getNumber(int row, int column) {
        return grid[row][column];
    }

    public void setNumber(int row, int column, int number) {
        grid[row][column] = number;
    }

    @Override
    public String toString() {
        return Arrays.deepToString(grid);
    }
}
