package com.example.hypermile.util;

import android.view.ViewGroup;

import java.sql.Timestamp;

public class Utils {
    private final static double GALLON_LITRE_CONVERSION = 4.54609;
    private final static double METRES_MILES_CONVERSION = 0.0006213712;
    private final static double KG_CO2_E_PER_LITRE_DIESEL = 2.71685;
    private final static double KG_CO2_E_PER_LITRE_PETROL = 2.46651;

    /**
     * @brief Sets clipChildren to false recursively on parent ViewGroups
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

    static public double litresUsedFromMpgDistance(double mpg, double distance) {
        if (distance == 0.0 || mpg == 0.0) return 0.0;
        return round2dp(mpg / distance * GALLON_LITRE_CONVERSION);
    }

    static public double metresToMiles(double distance) {
        return round2dp(distance * METRES_MILES_CONVERSION);
    }

    static public double round2dp(double value) {
        return Math.round(value * 100) / 100.0;
    }

    //https://www.bp.com/bptargetneutralnavapp/consumer/bpTN_Online%20Travel%20GHG%20Emissions%20Calculator_Updated%20Methodology%20Statement_01July2021.961acd71.pdf
    static public double kgCO2e(double litres, int fuelType) {
        double kg = litres * (fuelType == 1? KG_CO2_E_PER_LITRE_PETROL : KG_CO2_E_PER_LITRE_DIESEL);
        return round2dp(kg);
    }

    static public long millisDiff(Timestamp from, Timestamp to) {
        return to.getTime() - from.getTime();
    }
}
