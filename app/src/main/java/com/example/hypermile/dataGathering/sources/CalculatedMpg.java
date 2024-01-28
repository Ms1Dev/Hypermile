package com.example.hypermile.dataGathering.sources;


import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;
import com.example.hypermile.dataGathering.PollCompleteListener;

public class CalculatedMpg extends DataSource<Double> implements PollCompleteListener {
    final static private double UK_GALLON_CONVERSION = 0.21996923465436;
    final static private double MAX_MPG = 99.99;
    final static private double KPH_MPH_CONVERSION = 0.621371;
    boolean newSpeedData = false;
    boolean newFuelData = false;
    double milesPerHour;
    double litresPerHour;
    VehicleDataLogger speed;

    public CalculatedMpg(VehicleDataLogger speed, DataSource<Double> fuelRate) {

        speed.addDataInputListener( new DataInputObserver<Double>() {
            @Override
            public void incomingData(Double data) {
                newSpeedData(data);
            }
        });

        fuelRate.addDataInputListener(new DataInputObserver<Double>() {
            @Override
            public void incomingData(Double data) {
                newFuelData(data);
            }
        });
    }

    @Override
    public String getName() {
        return "MPG";
    }

    public void newSpeedData(double data) {
        milesPerHour = data * KPH_MPH_CONVERSION;
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

            double milesPerGallon = calcMpg(litresPerHour,milesPerHour);

            if (milesPerGallon > MAX_MPG) milesPerGallon = MAX_MPG;

            notifyObservers(milesPerGallon);
        }
    }

    public static double calcMpg(double litresPerHour, double milesPerHour) {
        double gallonsPerHour = litresPerHour * UK_GALLON_CONVERSION;
        return milesPerHour / gallonsPerHour;
    }

    @Override
    public void pollingComplete() {
        newFuelData = false;
        newSpeedData = false;
    }
}
