package com.example.hypermile.util;

import android.view.ViewGroup;

import java.sql.Timestamp;

/**
 * Static Utility class containing generic helper functions
 */
public class Utils {
    private final static double GALLON_LITRE_CONVERSION = 4.54609;
    private final static double METRES_MILES_CONVERSION = 0.0006213712;
    private final static double KG_CO2_E_PER_LITRE_DIESEL = 2.71685;
    private final static double KG_CO2_E_PER_LITRE_PETROL = 2.46651;

    /**
     * Sets clipChildren to false recursively on parent ViewGroups
     * @param viewGroup
     */
    static public void unclip(ViewGroup viewGroup) {
        viewGroup.setClipChildren(false);
        viewGroup.setEnabled(false);
        if (viewGroup.getParent() != null) {
            try {
                unclip((ViewGroup) viewGroup.getParent());
            }
            catch (ClassCastException e) {}
        }
    }

    /**
     * Returns the litres of fuel used to travel a certain distance at a specific MPG
     * @param mpg
     * @param distance
     * @return
     */
    static public double litresUsedFromMpgDistance(double mpg, double distance) {
        if (distance == 0.0 || mpg == 0.0) return 0.0;
        return round2dp(mpg / distance * GALLON_LITRE_CONVERSION);
    }

    /**
     * Converts metres to miles
     * @param distance
     * @return
     */
    static public double metresToMiles(double distance) {
        return round2dp(distance * METRES_MILES_CONVERSION);
    }

    /**
     * Returns a double rounded to 2 decimal places
     * @param value
     * @return
     */
    static public double round2dp(double value) {
        return Math.round(value * 100) / 100.0;
    }

    /**
     * Calculates the CO2 produced from using a specific amount of fuel.
     * Amount of CO2 is calculated using table from BP website at the
     * following link: https://www.bp.com/bptargetneutralnavapp/consumer/bpTN_Online%20Travel%20GHG%20Emissions%20Calculator_Updated%20Methodology%20Statement_01July2021.961acd71.pdf
     * @param litres
     * @param fuelType
     * @return
     */
    static public double kgCO2e(double litres, int fuelType) {
        double kg = litres * (fuelType == 1? KG_CO2_E_PER_LITRE_PETROL : KG_CO2_E_PER_LITRE_DIESEL);
        return round2dp(kg);
    }

    /**
     * Returns the millisecond difference between two timestamps
     * @param from
     * @param to
     * @return
     */
    static public long millisDiff(Timestamp from, Timestamp to) {
        return to.getTime() - from.getTime();
    }
}
