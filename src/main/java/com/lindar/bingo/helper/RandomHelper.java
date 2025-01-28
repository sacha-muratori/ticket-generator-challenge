package com.lindar.bingo.helper;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomHelper {

    private Random random;

    public RandomHelper() {
        initializeRandomWithSystemTime();
    }

    private void initializeRandomWithSystemTime() {
        // Initialize Random with system time for uniqueness
        long seed = System.currentTimeMillis();
        this.random = new Random(seed);
    }

    public Random getRandom() {
        // Re-Initialize Random
        initializeRandomWithSystemTime();

        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public int generateRandomNumber(int bound){
        // Re-Initialize Random
        initializeRandomWithSystemTime();

        // simply returns a random number between 0 and bound (excluded)
        return random.nextInt(bound);
    }
}
