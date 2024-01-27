package com.example.hypermile.reports;

import android.location.Location;
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
    String dateOfReport;
    Map<String, Map<String,Object>> journeyData = new HashMap<>();
    ArrayList<Location> route = new ArrayList<>();
    double avgMpg;
    double avgSpeed;
    double avgSpeedIncStationary;
    double totalDistance;
    double fuelUsed;

    public Report(String dateOfReport, Map<String, Object> journeyData) {
        this.dateOfReport = dateOfReport;
        double totalMpg = 0;
        int mpgDataCount = 0;

        for (Map.Entry<String, Object> entry : journeyData.entrySet()) {
            try {
                HashMap<String,Object> row = (HashMap<String, Object>) entry.getValue();
                this.journeyData.put(entry.getKey(), row);
                Double mpg = (Double) row.get("MPG");

                if (mpg != null && mpg > 0 && mpg < 99.99) {
                    totalMpg += mpg;
                    mpgDataCount++;
                }

            }
            catch (ClassCastException e) {
                Log.e("Err", "Report: " + "Journey " + dateOfReport + " stored in incorrect format", e);
            }
        }

        avgMpg = totalMpg / mpgDataCount;

    }

    public void processData() {
        double totalSpeed = 0;
        double totalSpeedIncStationary = 0;
        int speedDataCount = 0;

        if (journeyData != null) {
            Location prevLocation = null;
            for (Map<String, Object> row : journeyData.values()) {
                try {
                    String mpg = String.valueOf((Double) row.get("MPG"));
                    Map<String, Double> locationData = (HashMap) row.get("Location");
                    Location location = new Location(mpg);
                    location.setLatitude(locationData.get("latitude"));
                    location.setLongitude(locationData.get("longitude"));
                    location.setAltitude(locationData.get("altitude"));
                    route.add(location);

                    if (prevLocation != null) {
                        double distance = prevLocation.distanceTo(location);
                        totalDistance += distance;
                    }
                    prevLocation = location;

                } catch (ClassCastException | NullPointerException e) {
                    Log.d("TAG", "getRoute: " + e);
                }

                Double speed = (Double) row.get("speed");

                if (speed != null) {
                    if (speed > 0) {
                        totalSpeed += speed;
                        speedDataCount++;
                    }
                    totalSpeedIncStationary += speed;
                }

            }

            avgSpeed = totalSpeed / speedDataCount;
            avgSpeedIncStationary = totalSpeedIncStationary / journeyData.size();
            totalDistance *= METRES_MILES_CONVERSION;
            double gallonsUsed = avgMpg * totalDistance;
            fuelUsed = GALLON_LITRE_CONVERSION * gallonsUsed;
        }
    }

    public ArrayList<Location> getRoute() {
        return route;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public double getAvgSpeedIncStationary() {
        return avgSpeedIncStationary;
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

    public Map<String, Map<String, Object>> getJourneyData() {
        return journeyData;
    }

    public double getAvgMpg() {
        return avgMpg;
    }
}
