package com.lindar.bingo.helper;

import com.lindar.bingo.model.ColumnRange;
import com.lindar.bingo.model.Ticket;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StripHelper {

    public static final int TICKETS_STRIP_SIZE = 6;

    /**
     * DISPLAY METHODS
     */
    // Method to display all the tickets
    public static String displayTickets(List<Ticket> strip) {
        StringBuilder stripStr = new StringBuilder("");
        stripStr.append("----------------------- \n");

        // Iterate through each ticket in the strip
        for (int i = 0; i < strip.size(); i++) {
            stripStr.append("Ticket " + (i + 1) + ": \n");
            TicketHelper.displayTicket(strip.get(i), stripStr);
            stripStr.append("\n");
        }

        return stripStr.toString();
    }

    /**
     * VALIDATION METHODS
     */
    public boolean isStripOfTicketsValid(List<Ticket> strip) {
        // Tickets per column should have space for exactly the amount of numbers we have in each columnRange
        List<List<Integer>> unRandomizedColumns = Arrays.stream(ColumnRange.values())
                .map(ColumnRange::getNumbers)
                .collect(Collectors.toList());

        for (List<Integer> columnNumbers : unRandomizedColumns) {
            int col = 0;
            int availableSize = columnNumbers.size();
            for (int iTicket = 0; iTicket < TICKETS_STRIP_SIZE; iTicket++) {
                Ticket ticket = strip.get(iTicket);

                for (int row = 0; row < Ticket.ROWS; row++) {
                    if (ticket.getNumber(row, col) == -1) {
                        availableSize--;
                    }
                }
            }
            col++;

            if(availableSize != 0){
                return false;
            }
        }
        return true;
    }


}
