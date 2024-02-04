package com.example.hypermile.dataGathering.sources;

import com.example.hypermile.dataGathering.DataSource;
import com.example.hypermile.dataGathering.PollingElement;

import java.util.Random;

/**
 * Used for development.
 * Generates a random double between min and max values
 */
public class RandomGenerator extends DataSource<Double> implements PollingElement {
    int min, max;
    Random random;

    public RandomGenerator(int min, int max) {
        assert(max > min);
        this.min = min;
        this.max = max;
        random = new Random();
    }
    @Override
    public void sampleData() {
        double rand = random.nextDouble();
        double scaled = rand * (max - min);
        double offset = scaled + min;
        notifyObservers(offset);
    }

    @Override
    public String getName() {
        return "Random";
    }
}
