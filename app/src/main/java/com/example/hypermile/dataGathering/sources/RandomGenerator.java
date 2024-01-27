package com.example.hypermile.dataGathering.sources;

import com.example.hypermile.dataGathering.DataSource;
import com.example.hypermile.dataGathering.PollingElement;

import java.util.Random;

public class RandomGenerator extends DataSource<Double> implements PollingElement {
    int min, max;
    Random random;

    public RandomGenerator(int min, int max) {
        name = "Random";
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
}
