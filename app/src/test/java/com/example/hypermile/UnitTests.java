package com.example.hypermile;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.hypermile.dataGathering.sources.CalculatedMaf;
import com.example.hypermile.dataGathering.sources.CalculatedMpg;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class UnitTests {

    CalculatedMaf calculatedMaf;

//    @Test
//    public void calculateFuelRate() {
//        double fuelRate = CalculatedMaf.calculateFuelRate(1,1,1,1);
//
//        assertEquals(fuelRate, 1.0, 0.001);
//    }

    @Test
    public void calculateGasDensity_15C() {
        double gasDensity = CalculatedMaf.gasDensity(1, 15);

        assertEquals(0.001225, gasDensity, 0.00001);
    }

    @Test
    public void calculateGasDensity_0C() {
        double gasDensity = CalculatedMaf.gasDensity(1, 0);

        assertEquals(0.001293, gasDensity, 0.00001);
    }

    @Test
    public void calculateGasDensity_100C() {
        double gasDensity = CalculatedMaf.gasDensity(1, 100);

        assertEquals(0.0009461, gasDensity, 0.00001);
    }

    @Test
    public void calculateGasDensity_pressure() {
        double gasDensity = CalculatedMaf.gasDensity(0.7845566247224, 2);

        assertEquals(0.00100649, gasDensity, 0.00001);
    }

    @Test
    public void calculateMpg() {
        double mpg = CalculatedMpg.calcMpg(5, 62.1371);
        assertEquals(56.4962, mpg, 0.001);
    }

}