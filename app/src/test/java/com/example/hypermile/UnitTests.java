package com.example.hypermile;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.hypermile.dataGathering.sources.CalculatedFuelRate;
import com.example.hypermile.dataGathering.sources.CalculatedMaf;
import com.example.hypermile.dataGathering.sources.CalculatedMpg;
import com.example.hypermile.util.Utils;

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

    @Test
    public void gallonsToLitres() {
        double litres = Utils.gallonsToLitres(1);
        assertEquals(4.54609, litres, 0.001);
    }

    @Test
    public void litresUsedFromMpg() {
        double litres = Utils.litresUsedFromMpgDistance(30.0, 30.0);
        double expectedLitres = Utils.round2dp( Utils.gallonsToLitres(1) );
        assertEquals(expectedLitres, litres, 0.001);
    }

    @Test
    public void litresUsedFromMpg2() {
        double litres = Utils.litresUsedFromMpgDistance(30.0, 0.3);
        double expectedLitres = Utils.round2dp( Utils.gallonsToLitres(0.01) );
        assertEquals(expectedLitres, litres, 0.001);
    }

    @Test
    public void metresToMiles() {
        double metres = Utils.metresToMiles(1609.34);
        assertEquals(1, metres, 0.001);
    }


}