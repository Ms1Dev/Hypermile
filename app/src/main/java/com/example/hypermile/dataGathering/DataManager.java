package com.example.hypermile.dataGathering;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.hypermile.MainActivity;
import com.example.hypermile.UserAlert;
import com.example.hypermile.dataGathering.sources.CalculatedMaf;
import com.example.hypermile.dataGathering.sources.CurrentTimestamp;
import com.example.hypermile.dataGathering.sources.CalculatedFuelRate;
import com.example.hypermile.dataGathering.sources.CalculatedMpg;
import com.example.hypermile.dataGathering.sources.MassAirFlowSensor;
import com.example.hypermile.dataGathering.sources.VehicleDataLogger;
import com.example.hypermile.obd.Obd;
import com.example.hypermile.obd.Parameter;
import com.google.android.material.snackbar.Snackbar;

import java.sql.Timestamp;

public class DataManager {
    private static final String PREFERENCE_FILENAME = "Hypermile_preferences";
    private static final String FUELTYPE_PREFERENCE = "fuel_type";
    private static final String ENGINESIZE_PREFERENCE = "engine_size";
    private CurrentTimestamp currentTimestamp;
    private VehicleDataLogger engineSpeed;
    private VehicleDataLogger speed;
    private DataSource<Double> massAirFlow;
    private DataSource<Double> fuelRate;
    private CalculatedMpg calculatedMpg;
    private int fuelType = -1;
    private int engineCapacity = -1;
    private static DataManager instance;
    private Context context;
    private boolean initialised = false;
    private DataManager(){};
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void initialise(Context context) {
        if (!initialised) {
            this.context = context;

            getVehicleSpecs(Obd.getVin());

            if (fuelType == -1 || engineCapacity == -1) {
                ((MainActivity) context).alertUser(UserAlert.VEHICLE_SPEC_UNKNOWN);
            }


            Poller poller = new Poller(1);

            Parameter speedParameter = Obd.getPid("0D");
            if (speedParameter != null) {
                speed = new VehicleDataLogger(
                        speedParameter,
                        "Speed",
                        "MPH",
                        1,
                        1,
                        1
                );
                speed.setMaxValue(120);
            }

            Parameter rpmParameter = Obd.getPid("0C");
            if (rpmParameter != null) {
                engineSpeed = new VehicleDataLogger(
                        rpmParameter,
                        "Engine Speed",
                        "RPM",
                        256,
                        4,
                        2
                );
                speed.setMaxValue(8000);
            }
            else {
                // TODO: warn user no RPM data
            }

            massAirFlow = getMassAirFlowSource(poller);

            if (massAirFlow != null) {
                fuelRate = new CalculatedFuelRate(massAirFlow);
            }
            else {
                // TODO: warn user no fuel statistics
            }

            if (fuelRate != null && speed != null) {
                calculatedMpg = new CalculatedMpg(speed, fuelRate);
            }
            else {
                // TODO: warn user no MPG
            }

            currentTimestamp = new CurrentTimestamp();

            poller.addPollingElement(engineSpeed);
            poller.addPollingElement(speed);

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

    private void getVehicleSpecs(String vin) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(vin, Context.MODE_PRIVATE);
        fuelType = sharedPreferences.getInt(FUELTYPE_PREFERENCE, -1);
        engineCapacity = sharedPreferences.getInt(ENGINESIZE_PREFERENCE, -1);

        if (fuelType == -1) {
            // try and get the fuel type from obd
            Parameter fuelTypeParam = Obd.getPid("51");
            if (fuelTypeParam != null) {
                fuelType = (int) fuelTypeParam.getData()[0];
            }
        }

        // if fuel type still not known or engine capacity is not known and obd does not support mass air flow then request vehicle details
        if ( fuelType == -1 || (engineCapacity == -1 && ( !(Obd.supportsPid("10") || Obd.supportsPid("66")) ) ) ) {

        }

    }

    private DataSource<Double> getMassAirFlowSource(Poller poller) {

        Parameter maf = Obd.getPid("10");
        if (maf != null) {
            VehicleDataLogger massAirFlow = new VehicleDataLogger(
                    maf,
                    "MAF",
                    "g/s",
                    256,
                    100,
                    2
            );
            poller.addPollingElement(massAirFlow);
            return massAirFlow;
        }

        // this is an alternative MAF sensor
        Parameter mafSensor = Obd.getPid("66");
        if (mafSensor != null) {
            VehicleDataLogger massAirFlow = new MassAirFlowSensor(
                    mafSensor,
                    "MAF",
                    "g/s",
                    256,
                    32,
                    5
            );
            poller.addPollingElement(massAirFlow);
            return massAirFlow;
        }

        // if no MAF sensors then do it the hard way
        Parameter manifoldPressure = Obd.getPid("0B");
        Parameter intakeTemperature = Obd.getPid("0F");

        if (manifoldPressure != null && intakeTemperature != null && engineSpeed != null) {
            VehicleDataLogger manifoldAbsolutePressure = new VehicleDataLogger(
                    manifoldPressure,
                    "Manifold Absolute Pressure",
                    "kPa",
                    1,
                    1,
                    1
            );

            VehicleDataLogger intakeAirTemperature = new VehicleDataLogger(
                    intakeTemperature,
                    "Intake Air Temperature",
                    "kPa",
                    -40
            );

            CalculatedMaf calculatedMaf = new CalculatedMaf(
                    manifoldAbsolutePressure,
                    intakeAirTemperature,
                    engineSpeed
            );
             poller.addPollingElement(manifoldAbsolutePressure);
             poller.addPollingElement(intakeAirTemperature);
             poller.addPollCompleteListener(calculatedMaf);

            return calculatedMaf;
        }

        return null;
    }

    public boolean isInitialised() {
        return initialised;
    }

    public DataSource<Double> getEngineSpeed() {
        return engineSpeed;
    }

    public DataSource<Double> getSpeed() {
        return speed;
    }

    public DataSource<Timestamp> getCurrentTimestamp() {
        return currentTimestamp;
    }

    public DataSource<Double> getMassAirFlow() {
        return massAirFlow;
    }

    public DataSource<Double> getFuelRate() {
        return fuelRate;
    }

    public DataSource<Double> getCalculatedMpg() {
        return calculatedMpg;
    }
}