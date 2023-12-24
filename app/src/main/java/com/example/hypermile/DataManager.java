package com.example.hypermile;


import com.example.hypermile.data.derivatives.CurrentTimestamp;
import com.example.hypermile.data.derivatives.CalculatedFuelRate;
import com.example.hypermile.data.derivatives.CalculatedMpg;
import com.example.hypermile.data.Poller;
import com.example.hypermile.data.derivatives.VehicleDataLogger;

public class DataManager {
    private CurrentTimestamp currentTimestamp;
    private VehicleDataLogger engineSpeed;
    private VehicleDataLogger massAirFlow;
    private VehicleDataLogger speed;
    private CalculatedMpg calculatedMpg;
    private CalculatedFuelRate calculatedFuelRate;
    private static DataManager instance;
    private boolean initialised = false;
    private DataManager(){};
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void initialise() {
        if (!initialised) {
            createVehicleDataLoggers();

            calculatedFuelRate = new CalculatedFuelRate(massAirFlow);
            calculatedMpg = new CalculatedMpg(speed, calculatedFuelRate);
            currentTimestamp = new CurrentTimestamp();

            Poller poller = new Poller(1);
            poller.addPollingElement(engineSpeed);
            poller.addPollingElement(massAirFlow);
            poller.addPollingElement(speed);

            poller.addPollCompleteListener(currentTimestamp);

            poller.start();

            initialised = true;
        }
    }

    public boolean isInitialised() {
        return initialised;
    }


    private void createVehicleDataLoggers() {
        engineSpeed = new VehicleDataLogger(
                "Engine Speed",
                "RPM",
                "010C\r",
                256,
                4,
                2
        );
        massAirFlow = new VehicleDataLogger(
                "MAF",
                "g/s",
                "0110\r",
                256,
                100,
                2
        );
        speed = new VehicleDataLogger(
                "Speed",
                "MPH",
                "010D\r",
                1,
                1,
                1
        );
    }

    public VehicleDataLogger getEngineSpeed() {
        return engineSpeed;
    }

    public VehicleDataLogger getMassAirFlow() {
        return massAirFlow;
    }

    public VehicleDataLogger getSpeed() {
        return speed;
    }

    public CalculatedMpg getDerivedMpg() {
        return calculatedMpg;
    }

    public CurrentTimestamp getCurrentTimestamp() {
        return currentTimestamp;
    }

    public CalculatedFuelRate getDerivedFuelRate() {
        return calculatedFuelRate;
    }

}
