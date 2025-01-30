package com.lindar.bingo.helper;

import com.lindar.bingo.model.Ticket;

import java.util.List;

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
}
