package com.example.hypermile.dataGathering.sources;

import android.location.Location;

import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;

/**
 * Calculates the current inclination of the vehicle using the current and previous GPS coordinates
 */
public class CalculatedInclination extends DataSource<Double> implements DataInputObserver<Location> {
    private Location prevLocation;

    public CalculatedInclination() {

    }
    @Override
    public void incomingData(Location data) {
        if (prevLocation != null) {
            double horizontalDistance = prevLocation.distanceTo(data);
            double verticalDistance = data.getAltitude() - prevLocation.getAltitude();
            double angleRadians = Math.atan(verticalDistance / horizontalDistance);
            double angleDegrees = Math.toDegrees(angleRadians);
            notifyObservers(angleDegrees);
        }
        prevLocation = data;
    }

    @Override
    public String getName() {
        return "Inclination";
    }
}
