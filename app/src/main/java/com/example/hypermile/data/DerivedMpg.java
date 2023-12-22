package com.example.hypermile.data;

import android.util.Log;

public class DerivedMpg extends DataPoint {
    final static private double UK_GALLON_CONVERSION = 0.21996923465436;
    boolean newSpeedData = false;
    boolean newFuelData = false;
    double milesPerHour;
    double litresPerHour;

    public DerivedMpg(VehicleDataLogger speed, DerivedFuelRate fuelRate) {
        speed.addDataInputListener( new DataInputObserver() {
            @Override
            public void incomingData(double data) {
                newSpeedData(data);
            }

            @Override
            public void setUnits(String units) {}
        });

        fuelRate.addDataInputListener(new DataInputObserver() {
            @Override
            public void incomingData(double data) {
                newFuelData(data);
            }

            @Override
            public void setUnits(String units) {}
        });
    }

    public void newSpeedData(double data) {
        milesPerHour = data;
        newSpeedData = true;
        calculateData();
    }

    public void newFuelData(double data) {
        litresPerHour = data;
        newFuelData = true;
        calculateData();
    }

    private void calculateData() {
        if (newFuelData && newSpeedData) {
            newFuelData = false;
            newSpeedData = false;

            double gallonsPerHour = litresPerHour * UK_GALLON_CONVERSION;
            double milesPerGallon = milesPerHour / gallonsPerHour;

            notifyObservers(milesPerGallon);
        }
    }
}
