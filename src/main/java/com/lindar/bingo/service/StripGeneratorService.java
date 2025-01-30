package com.lindar.bingo.service;

import com.lindar.bingo.helper.RandomHelper;
import com.lindar.bingo.helper.TicketHelper;
import com.lindar.bingo.model.ColumnRange;
import com.lindar.bingo.model.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.lindar.bingo.helper.StripHelper.TICKETS_STRIP_SIZE;
import static com.lindar.bingo.model.ColumnVariations.*;

@Service
public class StripGeneratorService {

    @Autowired
    private RandomHelper randomHelper;

    public List<Ticket> generateStrip(){
        // Create the Tickets Strip
        List<Ticket> strip = Stream.generate(Ticket::new)
                .limit(TICKETS_STRIP_SIZE)
                .collect(Collectors.toList());

        // Create the Numbers per Rows List (used to keep track of the MAX_ROW_NUMBERS rule)
        List<Integer> stripRowsNumbersCounter = new ArrayList<>(Collections.nCopies(TICKETS_STRIP_SIZE * Ticket.ROWS, 0));

        // Iterate through the Shuffled Columns
        for (int column = 0; column < Ticket.COLUMNS; column++) {

            try {
                // Generate and Shuffle numbers for this Column
                List<Integer> shuffledColumnNumbers = ColumnRange.values()[column].getNumbers();

                // Randomly select a variation based on the current Column (First, Middle, Last)
                List<Integer> stripColumnVariation = selectCurrentColumnVariation(column);

                // Total Sum of numbers in row pre-ticket iteration assignment (used for numbers distribution)
                int totalSum = stripRowsNumbersCounter.stream().mapToInt(Integer::intValue).sum();

                // Iterate through each ticket
                for (int iTicket = 0; iTicket < TICKETS_STRIP_SIZE; iTicket++) {
                    // Getting the Ticket
                    Ticket ticket = strip.get(iTicket);

                    // Select Row Indexes for this Ticket based on Strip re-balancing *** IMPORTANT LOGIC ***
                    List<Integer> rowsToFillIndexes = getRowsToFillIndexes(column, iTicket, totalSum,
                            stripColumnVariation, stripRowsNumbersCounter);

                    // Randomly choose 1, 2, or 3 numbers from the current Shuffled Column (in Ascending order)
                    List<Integer> numbersToAssign = selectNumbersToAssign(rowsToFillIndexes.size(), shuffledColumnNumbers);

                    // Assign the sorted numbers to the respective Rows in Ticket
                    for (int i = 0; i < rowsToFillIndexes.size(); i++) {
                        int rowToFill = rowsToFillIndexes.get(i);  // Get the row index to place the number
                        int numberToPlace = numbersToAssign.get(i); // Get the sorted number to place in the row
                        ticket.setNumber(rowToFill, column, numberToPlace); // Assign the number to the row and column
                    }
                }

            } catch (IllegalStateException e) {

                // Backtrack: Reset previous column and retry
                if (column > 0) {
                    // Clean-up Phase: clean this and 2 columns before
                    decreaseRowsCounterAndCleanUpTicketRows(stripRowsNumbersCounter, strip, column);
                    decreaseRowsCounterAndCleanUpTicketRows(stripRowsNumbersCounter, strip, column - 1);
                    decreaseRowsCounterAndCleanUpTicketRows(stripRowsNumbersCounter, strip, column - 2);

                    column = column - 3; // Move back to 2 columns + 1 to contrast for-cycle column++ on next iteration

                } else {
                    throw new RuntimeException("Unable to generate a valid strip");
                }
            }
        }

        return strip;
    }

    /**
     *** PRIVATE METHODS ***
     */
    private void decreaseRowsCounterAndCleanUpTicketRows(List<Integer> stripRowsNumbersCounter, List<Ticket> strip, int column) {
        // Initialize Row Index List
        List<Integer> decreaseTicketsRowsList = new ArrayList<>();

        // Iterate through each ticket
        for (int iTicket = 0; iTicket < TICKETS_STRIP_SIZE; iTicket++) {
            // Getting the Ticket
            Ticket ticket = strip.get(iTicket);

            // Save each index if row x column has a value
            for (int i = 0; i < Ticket.ROWS; i++) {
                int rowValue = ticket.getNumber(i, column);
                if(rowValue != 0){
                    decreaseTicketsRowsList.add((iTicket * 3) + i);
                    ticket.setNumber(i, column, 0);
                }
            }
        }

        // Decrease stripRowsNumbersCounter List based on the indexes in decreaseTicketsRowsList
        decreaseTicketsRowsList.forEach(index -> {
            stripRowsNumbersCounter.set(index, stripRowsNumbersCounter.get(index) - 1);
        });
    }

    private List<Integer> selectCurrentColumnVariation(int column) {
        List<Integer> stripColumnVariation;
        if (column == 0) {
            // FIRST COLUMN
            int variationKey = randomHelper.generateRandomNumber(VARIATIONS_FIRST_COLUMN.size());
            stripColumnVariation = new ArrayList<>(VARIATIONS_FIRST_COLUMN.get(variationKey));
        } else if (column == Ticket.COLUMNS - 1) {
            // LAST COLUMN
            int variationKey = randomHelper.generateRandomNumber(VARIATIONS_LAST_COLUMN.size());
            stripColumnVariation = new ArrayList<>(VARIATIONS_LAST_COLUMN.get(variationKey));
        } else {
            // MIDDLE COLUMN(S)
            int variationKey = randomHelper.generateRandomNumber(VARIATIONS_MIDDLE_COLUMNS.size());
            stripColumnVariation = new ArrayList<>(VARIATIONS_MIDDLE_COLUMNS.get(variationKey));
        }
        return stripColumnVariation;
    }

    private List<Integer> getRowsToFillIndexes(int column, int iTicket, int totalSum, List<Integer> stripColumnVariation, List<Integer> stripRowsNumbersCounter) {
        // Determine the offset for the current ticket's rows in the strip
        int ticketRowOffset = iTicket * 3; // Each ticket spans 3 rows

        // Get the rows for this ticket (relative to the strip)
        List<Integer> ticketRows = Arrays.asList(ticketRowOffset, ticketRowOffset + 1, ticketRowOffset + 2);

        // Select the actual Rows indexes randomly while balancing
        List<Integer> rowsToFillIndexes = new ArrayList<>();

        // Exclude duplicate variations
        List<Integer> remainingVariations = stripColumnVariation.stream()
                .distinct()
                .collect(Collectors.toList());

        // Balance Column Variations based on Rows Counter
        if (remainingVariations.size() > 1)
            balanceVariationsBasedOnRowsCounter(column, ticketRowOffset, totalSum, remainingVariations, stripRowsNumbersCounter);

        // Retry until a valid variation is found
        for (int attempt = 0; attempt < remainingVariations.size(); attempt++) {

            // 1) Get the number of Rows to fill for this Ticket (1, 2 or 3)
            int rowsToFill = remainingVariations.get(attempt);

            // 2) Pick rows with the least numbers already assigned
            ticketRows.sort(Comparator.comparingInt(stripRowsNumbersCounter::get));
            rowsToFillIndexes = ticketRows.subList(0, rowsToFill);

            // 3) Validate row counts for this variation
            boolean isValid = true;
            for (int rowIndex : rowsToFillIndexes) {
                if (stripRowsNumbersCounter.get(rowIndex) + 1 > Ticket.MAX_ROW_NUMBERS) {
                    isValid = false;
                    break;
                }
            }

            if (isValid) {
                // 4) If valid, update the row counts and commit the variation
                for (int rowIndex : rowsToFillIndexes) {
                    stripRowsNumbersCounter.set(rowIndex, stripRowsNumbersCounter.get(rowIndex) + 1);
                }
                // Remove used variation
                stripColumnVariation.remove(Integer.valueOf(rowsToFill));
                break; // Exit the for-loop once a valid variation is found

            } else {
                // 5) if we've tried all variations, stop retrying
                if (attempt == remainingVariations.size() - 1) {
                    throw new IllegalStateException("All variations exhausted for column " + column);
                }
            }
        }

        // Normalize row indexes to ticket-relative values (e.g., 0, 1, 2)
        rowsToFillIndexes.replaceAll(value -> value % 3);

        // Sort the selected rows in ascending order
        Collections.sort(rowsToFillIndexes);

        return rowsToFillIndexes;
    }

    private void balanceVariationsBasedOnRowsCounter(int column,  int ticketRowOffset, int totalSum, List<Integer> remainingVariations, List<Integer> stripRowsNumbersCounter) {
        int avgPer3Rows = totalSum / (stripRowsNumbersCounter.size() / 3); // Average per 3-row group

        if(avgPer3Rows > 0 && column >= 4){
            int currentSum = stripRowsNumbersCounter.get(ticketRowOffset)
                    + stripRowsNumbersCounter.get(ticketRowOffset + 1)
                    + stripRowsNumbersCounter.get(ticketRowOffset + 2);

            // If current sum is already 14 before we are on the last column iteration no variation would work on next
            if(column != Ticket.COLUMNS - 1 && currentSum == (Ticket.MAX_ROW_NUMBERS * Ticket.ROWS) - 1){
                throw new IllegalStateException("No Variation would work, rollback generation to previous column");
            }

            // If current sum is below average, prioritize larger numbers; otherwise, prioritize smaller numbers
            remainingVariations.sort(currentSum < avgPer3Rows ? Comparator.reverseOrder() : Comparator.naturalOrder());
        }
    }

    private List<Integer> selectNumbersToAssign(int rowsToFillSize, List<Integer> shuffledColumnNumbers) {
        List<Integer> numbersToAssign = new ArrayList<>();
        for (int i = 0; i < rowsToFillSize; i++) {
            // Choosing an index from the current Shuffled Column
            int randomIndex = randomHelper.generateRandomNumber(shuffledColumnNumbers.size());

            // Removing the number from the Shuffled Column and adding it to the List
            int numberToAssign = shuffledColumnNumbers.remove(randomIndex);
            numbersToAssign.add(numberToAssign);
        }

        // Sort the selected numbers in ascending order
        Collections.sort(numbersToAssign);

        return numbersToAssign;
    }
}
