package com.example.hypermile.data.derivatives;


import com.example.hypermile.data.CombinedDataSource;
import com.example.hypermile.data.DataInputObserver;
import com.example.hypermile.data.DataSource;
import com.example.hypermile.data.PollCompleteListener;
import com.example.hypermile.data.Poller;

import java.util.ArrayList;

public class CalculatedFuelRate extends DataSource<Double> implements PollCompleteListener, CombinedDataSource {
    ArrayList<VehicleDataLogger> constituents = new ArrayList<>();
    double manifoldAbsolutePressure;
    double intakeTemperature;
    double calculatedLoad;
    double engineSpeed;
    boolean hasManifoldAbsolutePressure;
    boolean hasIntakeTemperature;
    boolean hasCalculatedLoad;
    boolean hasEngineSpeed;


    public CalculatedFuelRate(VehicleDataLogger manifoldAbsolutePressure, VehicleDataLogger intakeTemperature,
                              VehicleDataLogger calculatedLoad, VehicleDataLogger engineSpeed) {

        constituents.add(manifoldAbsolutePressure);
        constituents.add(intakeTemperature);
        constituents.add(calculatedLoad);
        constituents.add(engineSpeed);

        manifoldAbsolutePressure.addDataInputListener( new DataInputObserver<Double>() {
            @Override
            public void incomingData(Double data) {
                newManifoldAbsolutePressureData(data);
            }

            @Override
            public void setUnits(String units) {}
        });

        intakeTemperature.addDataInputListener( new DataInputObserver<Double>() {
            @Override
            public void incomingData(Double data) {
                newIntakeTemperatureData(data);
            }

            @Override
            public void setUnits(String units) {}
        });

        calculatedLoad.addDataInputListener( new DataInputObserver<Double>() {
            @Override
            public void incomingData(Double data) {
                newCalculatedLoadData(data);
            }

            @Override
            public void setUnits(String units) {}
        });

        engineSpeed.addDataInputListener( new DataInputObserver<Double>() {
            @Override
            public void incomingData(Double data) {
                newEngineSpeedData(data);
            }

            @Override
            public void setUnits(String units) {}
        });
    }

    public void newManifoldAbsolutePressureData(double data) {
        manifoldAbsolutePressure = data;
        hasManifoldAbsolutePressure = true;
        calculateData();
    }

    public void newCalculatedLoadData(double data) {
        calculatedLoad = data;
        hasCalculatedLoad = true;
        calculateData();
    }

    public void newIntakeTemperatureData(double data) {
        intakeTemperature = data;
        hasIntakeTemperature = true;
        calculateData();
    }

    public void newEngineSpeedData(double data) {
        engineSpeed = data;
        hasEngineSpeed = true;
        calculateData();
    }

    private void calculateData() {
        if (hasManifoldAbsolutePressure && hasCalculatedLoad && hasIntakeTemperature && hasEngineSpeed) {

            // calc

//            notifyObservers(fuelRate);
        }
    }

    @Override
    public void pollingComplete() {
        hasManifoldAbsolutePressure = false;
        hasCalculatedLoad = false;
        hasIntakeTemperature = false;
        hasEngineSpeed = false;
    }

    @Override
    public void pollConstituents(Poller poller) {
        for (VehicleDataLogger vehicleDataLogger : constituents) {
            poller.addPollingElement(vehicleDataLogger);
        }
    }
}
