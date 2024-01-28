package com.example.hypermile.reports;

import android.location.Location;
import android.media.JetPlayer;
import android.util.Log;

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
    double avgSpeed;
    double avgSpeedIncStops;
    private final JourneyData journeyData;

    public Report(String dateOfReport, JourneyData journeyData) {
        this.dateOfReport = dateOfReport;
        this.journeyData = journeyData;
        totalDistance = Math.round((journeyData.getTotalDistanceMetres() * METRES_MILES_CONVERSION) * 100) / 100.0;
        fuelUsed = Math.round((journeyData.getAvgMpg() / totalDistance * GALLON_LITRE_CONVERSION) * 100) / 100.0;
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

    public double getAvgMpg() {
        return journeyData.getAvgMpg();
    }
}
