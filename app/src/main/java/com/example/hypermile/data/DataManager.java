package com.example.hypermile.data;


import android.util.Log;

import com.example.hypermile.data.derivatives.CalculatedFuelRate;
import com.example.hypermile.data.derivatives.CurrentTimestamp;
import com.example.hypermile.data.derivatives.MafCalculatedFuelRate;
import com.example.hypermile.data.derivatives.CalculatedMpg;
import com.example.hypermile.data.derivatives.VehicleDataLogger;
import com.example.hypermile.obd.Obd;
import com.example.hypermile.obd.Pid;

public class DataManager {
    private CurrentTimestamp currentTimestamp;
    private VehicleDataLogger engineSpeed;
    private VehicleDataLogger speed;
    private VehicleDataLogger calculatedEngineLoad;
    private VehicleDataLogger intakeAirTemperature;
    private VehicleDataLogger manifoldAbsolutePressure;
    private VehicleDataLogger fuelRateSensor;
    private VehicleDataLogger massAirFlow;
    private DataSource<Double> fuelRate;
    private CalculatedMpg calculatedMpg;
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

            Pid speedPid = Obd.getPid("0D");

            Log.d("TAG", "initialise: ." + speedPid.asString());

            if (speedPid != null) {
                speed = new VehicleDataLogger(
                        speedPid,
                        "Speed",
                        "MPH",
                        1,
                        1,
                        1
                );
            }

            Pid rpmPid = Obd.getPid("0C");
            if (rpmPid != null) {
                engineSpeed = new VehicleDataLogger(
                        rpmPid,
                        "Engine Speed",
                        "RPM",
                        256,
                        4,
                        2
                );
            }
            else {
                // TODO: warn user no RPM data
            }

            fuelRate = getFuelRateSource();

            if (fuelRate != null) {
                calculatedMpg = new CalculatedMpg(speed, fuelRate);
            }

            currentTimestamp = new CurrentTimestamp();

            Poller poller = new Poller(1);

            poller.addPollingElement(engineSpeed);
            poller.addPollingElement(speed);
            poller.addPollingElement(massAirFlow);
            poller.addPollingElement(manifoldAbsolutePressure);
            poller.addPollingElement(fuelRateSensor);
            poller.addPollingElement(intakeAirTemperature);

            poller.addPollCompleteListener(currentTimestamp);
            poller.addPollCompleteListener(calculatedMpg);
            try {
                poller.addPollCompleteListener((PollCompleteListener) fuelRate);
            }
            catch (ClassCastException e) {}

            poller.start();

            initialised = true;
        }
    }

    private DataSource<Double> getFuelRateSource() {
        // if there is fuel rate data take that first
        Pid fuelRate = Obd.getPid("5E");
        if (fuelRate != null) {
            fuelRateSensor = new VehicleDataLogger(
                    fuelRate,
                    "Fuel Rate",
                    "L/h",
                    256,
                    20,
                    2
            );
            return fuelRateSensor;
        }

        // if no fuel rate calculate it from MAF
        Pid maf = Obd.getPid("10");
        if (maf != null) {
            massAirFlow = new VehicleDataLogger(
                    maf,
                    "MAF",
                    "g/s",
                    256,
                    100,
                    2
            );
            return new MafCalculatedFuelRate(massAirFlow);
        }

        // this is an alternative MAF sensor
        Pid mafSensor = Obd.getPid("66");
        if (mafSensor != null) {
            massAirFlow = new VehicleDataLogger(
                    mafSensor,
                    "MAF",
                    "g/s",
                    256,
                    32,
                    5
            );
            return new MafCalculatedFuelRate(massAirFlow);
        }

        // if no fuel rate or MAF sensors then do it the hard way
        Pid manifoldPressure = Obd.getPid("0B");
        Pid intakeTemperature = Obd.getPid("0F");
        Pid calculatedLoad = Obd.getPid("04");

        if (manifoldPressure != null && intakeTemperature != null && calculatedLoad != null && engineSpeed != null) {
            manifoldAbsolutePressure = new VehicleDataLogger(
                    manifoldPressure,
                    "Manifold Absolute Pressure",
                    "kPa",
                    1,
                    1,
                    1
            );

            intakeAirTemperature = new VehicleDataLogger(
                    intakeTemperature,
                    "Intake Air Temperature",
                    "kPa",
                    -40
            );

            calculatedEngineLoad = new VehicleDataLogger(
                    calculatedLoad,
                    "Calculated Engine Load",
                    "kPa",
                    100,
                    255,
                    1
            );

            return new CalculatedFuelRate(
                    manifoldAbsolutePressure,
                    intakeAirTemperature,
                    calculatedEngineLoad,
                    engineSpeed
            );
        }

        return null;
    }

    public boolean isInitialised() {
        return initialised;
    }

    public VehicleDataLogger getEngineSpeed() {
        return engineSpeed;
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

    public VehicleDataLogger getCalculatedEngineLoad() {
        return calculatedEngineLoad;
    }

    public VehicleDataLogger getIntakeAirTemperature() {
        return intakeAirTemperature;
    }

    public VehicleDataLogger getManifoldAbsolutePressure() {
        return manifoldAbsolutePressure;
    }

    public VehicleDataLogger getFuelRateSensor() {
        return fuelRateSensor;
    }

    public VehicleDataLogger getMassAirFlow() {
        return massAirFlow;
    }

    public DataSource<Double> getFuelRate() {
        return fuelRate;
    }

    public CalculatedMpg getCalculatedMpg() {
        return calculatedMpg;
    }
}
