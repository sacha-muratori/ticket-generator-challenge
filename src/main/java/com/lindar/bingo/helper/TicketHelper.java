package com.lindar.bingo.helper;

import com.lindar.bingo.model.ColumnRange;
import com.lindar.bingo.model.Ticket;

import java.util.List;

public class TicketHelper {
    /**
     * DISPLAY METHODS
     */
    // Helper method to print a single ticket
    public static void displayTicket(Ticket ticket, StringBuilder stringBuilder) {
        // Iterate over the rows of the ticket
        for (int row = 0; row < Ticket.ROWS; row++) {
            // Iterate over the column of the ticket
            for (int col = 0; col < Ticket.COLUMNS; col++) {
                int number = ticket.getGrid()[row][col];

                // If last column do not add a comma
                String conjuction = col != Ticket.COLUMNS - 1 ? ", " : "";

                // Append the number or blank space to the row string
                stringBuilder.append((number == 0 ? "-" : String.format("%2d", number)) + conjuction);
            }
            stringBuilder.append("\n");
        }
    }

    /**
     * VALIDATION METHODS
     */
    public static boolean isTicketValid(Ticket ticket){
        return hasTicketValidNumbers(ticket) && hasTicketValidBlankSpaces(ticket);
    }

    public static boolean hasTicketValidNumbers(Ticket ticket) {
        // Condition 1) ticket has at max 5 numbers in same row and exactly 15 numbers in total
        Boolean condition1 = hasValidNumbersInRow(ticket);

        // Condition 2) ticket has ascending non-duplicate numbers per column in exact ColumnRange
        Boolean condition2 = hasValidNumbersInColumn(ticket);

        return condition1 && condition2;
    }

    private static Boolean hasValidNumbersInRow(Ticket ticket) {
        int totalTicketNumbersCounter = 0;
        for(int row = 0; row < Ticket.ROWS; row++){
            int numbersCounter = 0;

            for(int column = 0; column < Ticket.COLUMNS; column++){
                if(ticket.getNumber(row, column) != 0){
                    numbersCounter++;
                }
            }

            if(numbersCounter != Ticket.MAX_ROW_NUMBERS){
                return false;
            } else {
                totalTicketNumbersCounter += numbersCounter;
            }
        }
        return totalTicketNumbersCounter == Ticket.ROWS * Ticket.MAX_ROW_NUMBERS;
    }

    private static Boolean hasValidNumbersInColumn(Ticket ticket) {
        for(int column = 0; column < Ticket.COLUMNS; column++){
            List<Integer> columnRangeList = ColumnRange.values()[column].getNumbers();

            for(int row = 0; row < Ticket.ROWS; row++) {
                int number = ticket.getNumber(row, column);
                if (number != 0) {
                    if (columnRangeList.contains(number)) {
                        columnRangeList.remove(Integer.valueOf(number));
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean hasTicketValidBlankSpaces(Ticket ticket) {
        // Condition 1) ticket has at max 4 spaces in same row
        Boolean condition1 = hasValidBlankSpacesInRow(ticket);

        // Condition 2) ticket has at max 2 spaces in same column
        Boolean condition2 = hasValidBlankSpacesInColumn(ticket);

        return condition1 && condition2;
    }

    private static boolean hasValidBlankSpacesInRow(Ticket ticket) {
        for(int row = 0; row < Ticket.ROWS; row++){
            int blankCounter = 0;

            for(int column = 0; column < Ticket.COLUMNS; column++){
                if(ticket.getNumber(row, column) == 0){
                    blankCounter++;
                }
            }

            if(blankCounter != Ticket.MAX_ROW_BLANK_SPACES){
                return false;
            }
        }
        return true;
    }

    private static boolean hasValidBlankSpacesInColumn(Ticket ticket) {
        for(int column = 0; column < Ticket.COLUMNS; column++){
            int blankCounter = 0;

            for(int row = 0; row < Ticket.ROWS; row++){
                if(ticket.getNumber(row, column) == 0){
                    blankCounter++;
                }
            }

            if(blankCounter > Ticket.MAX_COLUMN_BLANK_SPACES){
                return false;
            }
        }
        return true;
    }
}
