package com.lindar.bingo;

import com.lindar.bingo.helper.TicketHelper;
import com.lindar.bingo.model.Ticket;
import com.lindar.bingo.service.StripGeneratorService;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class StripGeneratorServiceTest {

    @Autowired
    private StripGeneratorService stripGeneratorService;

    /**
     * Test 1: Verify ticket generation rules for 1 strip
     */
    @Test
    void testTicketGenerationValidation() {
        // Generate one strip
        generateAndValidateStrip();
    }

    /**
     * Test 2: Warm-up and performance test for 10 strips
     */
    @Test
    void testPerformanceFor10Strips() {
        warmUpJvm();

        long startTime = System.nanoTime();

        for (int i = 0; i < 10; i++) {
            generateAndValidateStrip();
        }

        long endTime = System.nanoTime();
        long durationInMs = (endTime - startTime) / 1_000_000;

        System.out.println("Time taken to generate 10 strips: " + durationInMs + " ms");
    }

    /**
     * Test 3: Warm-up and performance test for 100 strips
     */
    @Test
    void testPerformanceFor100Strips() {
        warmUpJvm();

        long startTime = System.nanoTime();

        for (int i = 0; i < 100; i++) {
            generateAndValidateStrip();
        }

        long endTime = System.nanoTime();
        long durationInMs = (endTime - startTime) / 1_000_000;

        System.out.println("Time taken to generate 100 strips: " + durationInMs + " ms");
    }

    /**
     * Test 4: Warm-up and performance test for 1k strips
     */
    @Test
    @Ignore
    void testPerformanceFor1kStrips() {
        warmUpJvm();

        long startTime = System.nanoTime();

        for (int i = 0; i < 1_000; i++) {
            generateAndValidateStrip();
        }

        long endTime = System.nanoTime();
        long durationInMs = (endTime - startTime) / 1_000_000;

        System.out.println("Time taken to generate 1,000 strips: " + durationInMs + " ms");
    }

    /**
     * Test 5: Warm-up and performance test for 10k strips
     */
    @Test
    @Ignore
    void testPerformanceFor10kStrips() {
        warmUpJvm();

        long startTime = System.nanoTime();

        for (int i = 0; i < 10_000; i++) {
            generateAndValidateStrip();
        }

        long endTime = System.nanoTime();
        long durationInMs = (endTime - startTime) / 1_000_000;

        System.out.println("Time taken to generate 10,000 strips: " + durationInMs + " ms");
    }

    /**
     * Test 6: Performance test for 100k strips
     */
    @Test
    @Ignore
    void testPerformanceFor100kStrips() {
        warmUpJvm();

        long startTime = System.nanoTime();

        for (int i = 0; i < 100_000; i++) {
            generateAndValidateStrip();
        }

        long endTime = System.nanoTime();
        long durationInMs = (endTime - startTime) / 1_000_000;

        System.out.println("Time taken to generate 100,000 strips: " + durationInMs + " ms");
    }

    /**
     * Warm-up the JVM by generating a few strips
     */
    private void generateAndValidateStrip() {
        List<Ticket> generatedStrip = stripGeneratorService.generateStrip();

        // Validate each ticket in the strip using TicketHelper
        for (Ticket ticket : generatedStrip) {
            assertTrue(TicketHelper.hasTicketValidBlankSpaces(ticket), "Invalid blank spaces in ticket.");
//            assertTrue(TicketHelper.hasTicketValidNumbers(ticket), "Invalid number distribution in ticket.");
        }
    }

    private void warmUpJvm() {
        System.out.println("Warming up JVM...");
        for (int i = 0; i < 1_000_000; i++) {
            Math.sqrt(i); // Perform some lightweight operation
        }
        System.out.println("JVM warmed up.");
    }
}
