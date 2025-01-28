package com.lindar.bingo.helper;

import com.lindar.bingo.model.Ticket;

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
