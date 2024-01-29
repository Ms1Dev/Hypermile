package com.example.hypermile.reports;

import android.location.Location;
import android.media.JetPlayer;
import android.util.Log;

import com.example.hypermile.util.Utils;
import com.google.type.LatLng;

import org.checkerframework.checker.units.qual.A;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Report implements Serializable {
    private final static double METRES_MILES_CONVERSION = 0.0006213712;
    private final static double GALLON_LITRE_CONVERSION = 4.54609;
    final static private double KPH_MPH_CONVERSION = 0.621371;
    String dateOfReport;
    double totalDistance;
    double fuelUsed;
    double fuelUsedIncStops;
    double avgSpeed;
    double avgSpeedIncStops;
    private final JourneyData journeyData;

    public Report(String dateOfReport, JourneyData journeyData) {
        this.dateOfReport = dateOfReport;
        this.journeyData = journeyData;
        totalDistance = Utils.metresToMiles(journeyData.getTotalDistanceMetres());
        fuelUsed = Utils.litresUsedFromMpgDistance(journeyData.getAvgMpg(), totalDistance);
        fuelUsedIncStops = Utils.round2dp(journeyData.getFuelUsed());
        avgSpeed = Math.round((journeyData.getAvgSpeed() * KPH_MPH_CONVERSION) * 100) / 100.0;
        avgSpeedIncStops = Math.round((journeyData.getAvgSpeedIncStops() * KPH_MPH_CONVERSION) * 100) / 100.0;
    }

    public ArrayList<Map<String,Double>> getRoute() {
        return journeyData.getRoute();
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public double getAvgSpeedIncStationary() {
        return avgSpeedIncStops;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getFuelUsed() {
        return fuelUsed;
    }

    public String getDateOfReport() {
        return dateOfReport;
    }

    public double getFuelUsedIncStops() {
        return fuelUsedIncStops;
    }

    public double getAvgMpg() {
        return  Utils.round2dp(journeyData.getAvgMpg());
    }
}
