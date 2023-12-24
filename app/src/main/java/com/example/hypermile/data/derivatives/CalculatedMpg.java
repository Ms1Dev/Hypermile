package com.example.hypermile.data.derivatives;


import com.example.hypermile.data.CombinedDataSource;
import com.example.hypermile.data.DataInputObserver;
import com.example.hypermile.data.DataSource;
import com.example.hypermile.data.PollCompleteListener;
import com.example.hypermile.data.Poller;

public class CalculatedMpg extends DataSource<Double> implements PollCompleteListener {
    final static private double UK_GALLON_CONVERSION = 0.21996923465436;
    final static private double MAX_MPG = 99.99;
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

            @Override
            public void setUnits(String units) {}
        });

        fuelRate.addDataInputListener(new DataInputObserver<Double>() {
            @Override
            public void incomingData(Double data) {
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

            if (milesPerGallon > MAX_MPG) milesPerGallon = MAX_MPG;

            notifyObservers(milesPerGallon);
        }
    }

    @Override
    public void pollingComplete() {
        newFuelData = false;
        newSpeedData = false;
    }
}
