package com.lindar.bingo.service;

import com.lindar.bingo.helper.RandomHelper;
import com.lindar.bingo.helper.TicketHelper;
import com.lindar.bingo.model.ColumnRange;
import com.lindar.bingo.model.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
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

        // Generate and Shuffle numbers for each Column
        List<List<Integer>> shuffledColumns = Arrays.stream(ColumnRange.values())
                .map(ColumnRange::getNumbers)
                .map(numbers -> {
                    Collections.shuffle(numbers, randomHelper.getRandom());
                    return numbers;
                })
                .collect(Collectors.toList());

        // Create the Numbers per Rows List (used to keep track of the MAX_ROW_NUMBERS rule)
        List<Integer> stripRowsNumbersCounter = new ArrayList<>(Collections.nCopies(TICKETS_STRIP_SIZE * Ticket.ROWS, 0));

        boolean correctStrip = false;
        while(!correctStrip){

            // Limit retries to 100 attempts
            int retryCount = 0; int retryLimit = 100;

            // Iterate through the Shuffled Columns
            for (int column = 0; column < shuffledColumns.size(); column++) {

                boolean success = false;
                while (!success) {
                    // Increase retry count
                    retryCount += 1;

                    try {
                        // Select Shuffled Column
                        List<Integer> shuffledColumnNumbers = shuffledColumns.get(column);

                        // Randomly select a variation based on the current Column (First, Middle, Last)
                        List<Integer> stripColumnVariation = selectCurrentColumnVariation(column, shuffledColumns.size());

                        // Iterate through each ticket
                        for (int iTicket = 0; iTicket < TICKETS_STRIP_SIZE; iTicket++) {
                            // Getting the Ticket
                            Ticket ticket = strip.get(iTicket);

                            // Select Row Indexes for this Ticket based on Strip re-balancing *** IMPORTANT LOGIC ***
                            List<Integer> rowsToFillIndexes = getRowsToFillIndexes(column, iTicket, stripColumnVariation, stripRowsNumbersCounter);

                            // Randomly choose 1, 2, or 3 numbers from the current Shuffled Column (in Ascending order)
                            List<Integer> numbersToAssign = selectNumbersToAssign(rowsToFillIndexes.size(), shuffledColumnNumbers);

                            // Assign the sorted numbers to the respective Rows in Ticket
                            for (int i = 0; i < rowsToFillIndexes.size(); i++) {
                                int rowToFill = rowsToFillIndexes.get(i);  // Get the row index to place the number
                                int numberToPlace = numbersToAssign.get(i); // Get the sorted number to place in the row
                                ticket.setNumber(rowToFill, column, numberToPlace); // Assign the number to the row and column
                            }
                        }

                        success = true; // Exit the loop if column generation succeeds

                    } catch (IllegalStateException e) {

                        // Exceeded maximum retries (while gets here when an exception is thrown in getRowsToFillIndexes)
                        if (retryCount >= retryLimit) {
                            // Reset the entire strip and start over
                            return generateStrip(); // Restart the process from scratch

//                            throw new RuntimeException("Exceeded maximum retries for column " + column + "."
//                                    + " Unable to generate a valid strip.");
                        }

                        // Backtrack: Reset previous column and retry
                        if (column > 0) {
                            // Clean-up Phase: current column
                            restoreAndReshuffleColumn(shuffledColumns, column);
                            decreaseStripRowsNumbersCounterBasedOnColumnIndex(stripRowsNumbersCounter, strip, column);
                            cleanUpTicketsRowsBasedOnColumnIndex(strip, column);

                            column--; // Move back to the previous column

                            // Clean-up Phase: previous column
                            restoreAndReshuffleColumn(shuffledColumns, column);
                            decreaseStripRowsNumbersCounterBasedOnColumnIndex(stripRowsNumbersCounter, strip, column);
                            cleanUpTicketsRowsBasedOnColumnIndex(strip, column);
                        } else {
                            throw new RuntimeException("Unable to generate a valid strip");
                        }
                    }
                }
            }

            // Re-run whole strip generation until all tickets are correctly formed
            correctStrip = validateStrip(strip);
        }

        // Debug Line
//        System.out.println(StripHelper.displayTickets(strip));

        return strip;
    }

    /**
     *** PRIVATE METHODS ***
     */
    private void cleanUpTicketsRowsBasedOnColumnIndex(List<Ticket> strip, int column) {
        // Iterate through each ticket
        for (int iTicket = 0; iTicket < TICKETS_STRIP_SIZE; iTicket++) {
            // Getting the Ticket
            Ticket ticket = strip.get(iTicket);

            // Clean-up each row x column value
            for (int i = 0; i < 3; i++) {
                ticket.setNumber(i, column, 0);
            }
        }
    }

    private void decreaseStripRowsNumbersCounterBasedOnColumnIndex(List<Integer> stripRowsNumbersCounter, List<Ticket> strip, int column) {
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
                }
            }
        }

        // Decrease stripRowsNumbersCounter List based on the indexes in decreaseTicketsRowsList
        decreaseTicketsRowsList.forEach(index -> {
            if (index >= 0 && index < stripRowsNumbersCounter.size()) {
                stripRowsNumbersCounter.set(index, stripRowsNumbersCounter.get(index) - 1);
            }
        });
    }

    public void restoreAndReshuffleColumn(List<List<Integer>> shuffledColumns, int column) {
        // Get the range of numbers from the ColumnRange enum
        List<Integer> columnValues = ColumnRange.values()[column].getNumbers();

        // Restore the list at the given column index with the values from ColumnRange
        shuffledColumns.set(column, new ArrayList<>(columnValues));

        // Shuffle the restored list
        Collections.shuffle(shuffledColumns.get(column), randomHelper.getRandom());
    }

    private List<Integer> selectCurrentColumnVariation(int column, int shuffledColumnsSize) {
        List<Integer> stripColumnVariation;
        if (column == 0) {
            // FIRST COLUMN
            int variationKey = randomHelper.generateRandomNumber(VARIATIONS_FIRST_COLUMN.size());
            stripColumnVariation = new ArrayList<>(VARIATIONS_FIRST_COLUMN.get(variationKey));
        } else if (column == shuffledColumnsSize - 1) {
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

    private List<Integer> getRowsToFillIndexes(int column, int iTicket, List<Integer> stripColumnVariation, List<Integer> stripRowsNumbersCounter) {
        // What's the idea?
        // The idea is that for this ticket (based on re-balancing) I am going to find out which stripColumnVariation is more suitable.
        // If I have a middle variation [1, 1, 1, 2, 2, 3], and I shuffle it randomly into [1, 3, 1, 2, 1, 2],
        // based on current logic I am supposed to pick a number through the iTicket. So will always take 0 for ticket 1, 1 for ticket 2 etc.
        // The assumption is now that any of those numbers can be placed in a ticket row as long as they don't pass the MAX_ROW_NUMBERS rule.
        // If they do is good, if they don't I need to re-shuffle it.
        // But this is wrong because on the next iteration for ticket 2, if the variation number is not good I will be shuffling it, and
        // potentially I'm breaking the variation values. Like picking up randomly a 1 for 6 times and never use a 3 which is wrong.
        // So I need to also keep track or discard the variation numbers used.
        // I am choosing to remove it from the stripColumnVariation.

        // Let's try

        // Determine the offset for the current ticket's rows in the strip
        int ticketRowOffset = iTicket * 3; // Each ticket spans 3 rows

        // Get the rows for this ticket (relative to the strip)
        List<Integer> ticketRows = Arrays.asList(ticketRowOffset, ticketRowOffset + 1, ticketRowOffset + 2);

        // Select the actual Rows indexes randomly while balancing
        List<Integer> rowsToFillIndexes;

        // Limit retries to 100 attempts per column
        int columnVariationRetryCount = 0; int columnVariationRetryLimit = 100;
        Set<Integer> usedVariations = new HashSet<>(); // Track the variations that have been refused

        while (true) { // Retry until a valid variation is found
            // 0) clean-up
//            rowsToFillIndexes = new ArrayList<>();

            // 1) Randomize Values in selected Column Variation
            Collections.shuffle(stripColumnVariation, randomHelper.getRandom());

            // 2) Get the number of Rows to fill for this Ticket (1, 2 or 3)
            int rowsToFill = stripColumnVariation.get(0);

            // 3) Pick rows with the least numbers already assigned
            ticketRows.sort(Comparator.comparingInt(stripRowsNumbersCounter::get));
            rowsToFillIndexes = ticketRows.subList(0, rowsToFill);

            // 4) Validate row counts for this variation
            boolean isValid = true;
            for (int rowIndex : rowsToFillIndexes) {
                if (stripRowsNumbersCounter.get(rowIndex) + 1 > Ticket.MAX_ROW_NUMBERS) {
                    isValid = false;
                    break;
                }
            }

            // 5) If valid, update the row counts and commit the variation
            if (isValid) {
                for (int rowIndex : rowsToFillIndexes) {
                    // Update the stripRowsNumbersCounter list
                    stripRowsNumbersCounter.set(rowIndex, stripRowsNumbersCounter.get(rowIndex) + 1);
                }
                // Remove used variation
                stripColumnVariation.remove(0);
                break; // Exit the while loop
            }

            // 6) If we've tried all variations, stop retrying
            usedVariations.add(stripColumnVariation.get(0));
            Set<Integer> remainingVariations = new HashSet<>(stripColumnVariation); // Track remaining variations without duplicates
            remainingVariations.removeAll(usedVariations); // Remove the used ones

            if (remainingVariations.isEmpty()) {
                throw new IllegalStateException("All variations exhausted for column " + column);
            }

            // 7) If we retried more than 100 times, stop retrying
            columnVariationRetryCount++;

            if (columnVariationRetryCount >= columnVariationRetryLimit) {
                throw new IllegalStateException("Unable to find a valid variation after " + columnVariationRetryLimit + " attempts for column " + column);
            }
        }

        // Normalize row indexes to ticket-relative values (e.g., 0, 1, 2)
        rowsToFillIndexes.replaceAll(value -> value % 3);

        // Sort the selected rows in ascending order
//        Collections.sort(rowsToFillIndexes);

        return rowsToFillIndexes;
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

    private boolean validateStrip(List<Ticket> strip) {
        int correctTicketCounter = 0;
        for (int iTicket = 0; iTicket < TICKETS_STRIP_SIZE; iTicket++) {
            if(TicketHelper.hasTicketValidBlankSpaces(strip.get(iTicket))){
                correctTicketCounter++;
            } else {
                break;
            }
        }

        if(!(correctTicketCounter == TICKETS_STRIP_SIZE)){
            return false;
        }
        return true;
    }
}
