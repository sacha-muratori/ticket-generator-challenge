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
            strip = Stream.generate(Ticket::new).limit(TICKETS_STRIP_SIZE).collect(Collectors.toList());

            List<Integer> stripRowsNumbersCounter = new ArrayList<>(Collections.nCopies(TICKETS_STRIP_SIZE * Ticket.ROWS, 0));

            try {
                for (int column = 0; column < Ticket.COLUMNS; column++) {
                    List<Integer> shuffledColumnNumbers = new ArrayList<>(ColumnRange.values()[column].getNumbers());
                    Collections.shuffle(shuffledColumnNumbers, randomHelper.getRandom());

                    List<Integer> columnVariation = getColumnVariation(column);
                    columnVariation = orderVariationBasedOnRows(columnVariation, stripRowsNumbersCounter);

                    for (int iTicket = 0; iTicket < TICKETS_STRIP_SIZE; iTicket++) {
                        Ticket ticket = strip.get(iTicket);
                        List<Integer> rowsToFill = getRowsToFill(iTicket, columnVariation, stripRowsNumbersCounter);
                        List<Integer> numbersToAssign = selectNumbers(rowsToFill.size(), shuffledColumnNumbers);
                        for (int i = 0; i < rowsToFill.size(); i++) {
                            ticket.setNumber(rowsToFill.get(i), column, numbersToAssign.get(i));
                        }
                    }
                }
            } catch (Exception e){
                continue; // next while iteration
            }
            isValidStrip = strip.stream().allMatch(TicketHelper::isTicketValid);
        } while (!isValidStrip);

        return strip;
    }

    /**
     *** PRIVATE METHODS ***
     */
    private List<Integer> getColumnVariation(int column) {
        Map<Integer, List<Integer>> variations
                = column == 0 ? VARIATIONS_FIRST_COLUMN
                : column == Ticket.COLUMNS - 1 ? VARIATIONS_LAST_COLUMN
                : VARIATIONS_MIDDLE_COLUMNS;
        return new ArrayList<>(variations.get(randomHelper.generateRandomNumber(variations.size())));
    }

    private List<Integer> orderVariationBasedOnRows(List<Integer> columnVariation, List<Integer> stripRowsNumbersCounter) {
        Map<Integer, Integer> rowUsageMap = IntStream.range(0, stripRowsNumbersCounter.size())
                .boxed()
                .collect(Collectors.toMap(i -> i / 3, stripRowsNumbersCounter::get, Integer::sum));

        List<Integer> sortedRowIndexes = rowUsageMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())  // Ascending order
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

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

        int rowsToFill = columnVariation.get(iTicket);  // 1, 2, or 3
        Collections.shuffle(ticketRows, randomHelper.getRandom());

        List<Integer> sortedRows = ticketRows.stream()
                .sorted(Comparator.comparingInt(stripRowsNumbersCounter::get))  // Sort by row usage
                .limit(rowsToFill)  // Select the required number of rows
                .collect(Collectors.toList());

        for (int rowIndex : sortedRows) {
            int currentCount = stripRowsNumbersCounter.get(rowIndex);

            if(currentCount == Ticket.MAX_ROW_NUMBERS){
                throw new IllegalStateException("Row has already 5 numbers");
            } else {
                stripRowsNumbersCounter.set(rowIndex, stripRowsNumbersCounter.get(rowIndex) + 1);
            }
        }
        return sortedRows.stream().map(row -> row % 3).sorted().collect(Collectors.toList());
    }

    private List<Integer> selectNumbers(int count, List<Integer> shuffledColumnNumbers) {
        List<Integer> selectedNumbers = new ArrayList<>(shuffledColumnNumbers.subList(0, count));
        shuffledColumnNumbers.subList(0, count).clear();
        Collections.sort(selectedNumbers);
        return selectedNumbers;
    }
}
