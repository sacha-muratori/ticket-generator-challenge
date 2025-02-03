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
        List<Ticket> strip;
        Boolean isValidStrip = false;
        do {
            // Create the Tickets Strip
            strip = Stream.generate(Ticket::new).limit(TICKETS_STRIP_SIZE).collect(Collectors.toList());

            // Create the Numbers per Rows List (used to keep track of the MAX_ROW_NUMBERS rule)
            List<Integer> stripRowsNumbersCounter = new ArrayList<>(Collections.nCopies(TICKETS_STRIP_SIZE * Ticket.ROWS, 0));

            try {
                // Iterate through the Shuffled Columns
                for (int column = 0; column < Ticket.COLUMNS; column++) {
                    // Generate and Shuffle numbers for this Column
                    List<Integer> shuffledColumnNumbers = new ArrayList<>(ColumnRange.values()[column].getNumbers());
                    Collections.shuffle(shuffledColumnNumbers, randomHelper.getRandom());

                    // Randomly select a variation based on the current Column (First, Middle, Last)
                    List<Integer> columnVariation = getColumnVariation(column);

                    // Now reorder the variation based on the least used rows in stripRowsNumbersCounter
                    columnVariation = orderVariationBasedOnRows(columnVariation, stripRowsNumbersCounter);

                    // Iterate through each ticket
                    for (int iTicket = 0; iTicket < TICKETS_STRIP_SIZE; iTicket++) {
                        // Getting the Ticket
                        Ticket ticket = strip.get(iTicket);

                        // Select Row Indexes for this Ticket
                        List<Integer> rowsToFill = getRowsToFill(iTicket, columnVariation, stripRowsNumbersCounter);

                        // Randomly choose 1, 2, or 3 numbers from the current Shuffled Column (in Ascending order)
                        List<Integer> numbersToAssign = selectNumbers(rowsToFill.size(), shuffledColumnNumbers);

                        // Assign the sorted numbers to the respective Rows in Ticket
                        for (int i = 0; i < rowsToFill.size(); i++) {
                            ticket.setNumber(rowsToFill.get(i), column, numbersToAssign.get(i));
                        }
                    }
                }
            } catch (Exception e){
                continue; // next while iteration
            }

            // Validate Strip
            isValidStrip = strip.stream().allMatch(TicketHelper::isTicketValid);
        } while (!isValidStrip);

        return strip;
    }

    /**
     *** PRIVATE METHODS ***
     */
    private List<Integer> getColumnVariation(int column) {
        // Choosing Map Variations based on column and randomly choosing one of the variation lists
        Map<Integer, List<Integer>> variations
                = column == 0 ? VARIATIONS_FIRST_COLUMN
                : column == Ticket.COLUMNS - 1 ? VARIATIONS_LAST_COLUMN
                : VARIATIONS_MIDDLE_COLUMNS;

        return new ArrayList<>(variations.get(randomHelper.generateRandomNumber(variations.size())));
    }

    private List<Integer> orderVariationBasedOnRows(List<Integer> columnVariation, List<Integer> stripRowsNumbersCounter) {
        // Create a map where each row index holds the sum of its assigned numbers
        Map<Integer, Integer> rowUsageMap = IntStream.range(0, stripRowsNumbersCounter.size())
                .boxed()
                .collect(Collectors.toMap(i -> i / 3, stripRowsNumbersCounter::get, Integer::sum));

        // Sort the row indexes in ASCENDING order (the least filled rows first)
        List<Integer> sortedRowIndexes = rowUsageMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())  // Ascending order
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Order the columnVariation based on sortedRowIndexes (higher variations appearing first)
        List<Integer> orderedVariation = new ArrayList<>(columnVariation);
        int variationIndex = columnVariation.size() - 1;
        for (int rowIndex : sortedRowIndexes) {
            if (variationIndex >= 0) {
                orderedVariation.set(rowIndex, columnVariation.get(variationIndex--));
            }
        }

        return orderedVariation;
    }

    private List<Integer> getRowsToFill(int iTicket, List<Integer> columnVariation, List<Integer> stripRowsNumbersCounter) {
        int ticketRowOffset = iTicket * 3;
        List<Integer> ticketRows = new ArrayList<>(List.of(ticketRowOffset, ticketRowOffset + 1, ticketRowOffset + 2));

        // Get how many rows we need to fill for this ticket from columnVariation
        int rowsToFill = columnVariation.get(iTicket);  // 1, 2, or 3

        // Shuffle the rows before sorting by usage count to add randomness
        Collections.shuffle(ticketRows, randomHelper.getRandom());

        // Pick the `rowsToFill` lowest-count rows to maintain balance
        List<Integer> sortedRows = ticketRows.stream()
                .sorted(Comparator.comparingInt(stripRowsNumbersCounter::get))  // Sort by row usage
                .limit(rowsToFill)  // Select the required number of rows
                .collect(Collectors.toList());

        // Update the stripRowsNumbersCounter for the selected rows
        for (int rowIndex : sortedRows) {
            int currentCount = stripRowsNumbersCounter.get(rowIndex);

            if(currentCount == Ticket.MAX_ROW_NUMBERS){
                throw new IllegalStateException("Row has already 5 numbers");
            } else {
                stripRowsNumbersCounter.set(rowIndex, stripRowsNumbersCounter.get(rowIndex) + 1);
            }
        }

        // Normalize to local ticket row indexes (0, 1, 2) and sort rows
        return sortedRows.stream().map(row -> row % 3).sorted().collect(Collectors.toList());
    }

    private List<Integer> selectNumbers(int count, List<Integer> shuffledColumnNumbers) {
        // Select (and remove) Numbers from shuffledColumnNumbers, sort them for ASCENDING order
        List<Integer> selectedNumbers = new ArrayList<>(shuffledColumnNumbers.subList(0, count));
        shuffledColumnNumbers.subList(0, count).clear();
        Collections.sort(selectedNumbers);

        return selectedNumbers;
    }
}
