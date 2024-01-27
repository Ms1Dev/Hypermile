package com.example.hypermile.dataGathering;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import androidx.preference.PreferenceManager;

import com.example.hypermile.MainActivity;
import com.example.hypermile.UserAlert;
import com.example.hypermile.dataGathering.sources.CalculatedInclination;
import com.example.hypermile.dataGathering.sources.CalculatedMaf;
import com.example.hypermile.dataGathering.sources.CurrentLocation;
import com.example.hypermile.dataGathering.sources.CurrentTimestamp;
import com.example.hypermile.dataGathering.sources.CalculatedFuelRate;
import com.example.hypermile.dataGathering.sources.CalculatedMpg;
import com.example.hypermile.dataGathering.sources.MassAirFlowSensor;
import com.example.hypermile.dataGathering.sources.RandomGenerator;
import com.example.hypermile.dataGathering.sources.VehicleDataLogger;
import com.example.hypermile.obd.Obd;
import com.example.hypermile.obd.Parameter;
import java.sql.Timestamp;

public class DataManager implements EngineSpec {
    private static final String FUELTYPE_PREFERENCE = "fuelType";
    private static final String ENGINESIZE_PREFERENCE = "engineSize";
    final static int DEFAULT_DISPLACEMENT = 1600;
    final static int DEFAULT_FUELTYPE = 1;
    private CurrentTimestamp currentTimestamp;
    private VehicleDataLogger engineSpeed;
    private VehicleDataLogger speed;
    private DataSource<Double> massAirFlow;
    private CalculatedFuelRate fuelRate;
    private CalculatedMpg calculatedMpg;
    private CurrentLocation currentLocation;
    private CalculatedInclination calculatedInclination;
    private RandomGenerator randomGenerator;
    private int fuelType = -1;
    private int engineCapacity = -1;
    private Context context;
    private boolean initialised = false;
    Obd obd;
    private static boolean engineCapacityRequired = true;

    public DataManager(Context context){
        this.context = context;
    };

    public void initialise(Obd obd) {
        if (!initialised) {
            this.obd = obd;

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            getVehicleSpecs(sharedPreferences);

            sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String preference) {
                    if (preference.equals(ENGINESIZE_PREFERENCE) || preference.equals(FUELTYPE_PREFERENCE)) {
                        getVehicleSpecs(sharedPreferences);
                    }
                }
            });

            Poller poller = new Poller(1);

            Parameter speedParameter = obd.getPid("0D");
            if (speedParameter != null) {
                speed = new VehicleDataLogger(
                        speedParameter,
                        "Speed",
                        "KPH",
                        1,
                        1,
                        1
                );
                speed.setMaxValue(120);
            }

            Parameter rpmParameter = obd.getPid("0C");
            if (rpmParameter != null) {
                engineSpeed = new VehicleDataLogger(
                        rpmParameter,
                        "Engine Speed",
                        "RPM",
                        256,
                        4,
                        2
                );
                engineSpeed.setMaxValue(8000);
            }
            else {
                // TODO: warn user no RPM data
            }

            massAirFlow = getMassAirFlowSource(poller);

            if (massAirFlow != null) {
                fuelRate = new CalculatedFuelRate(massAirFlow);
                massAirFlow.setDecimalPoints(2);
            }
            else {
                // TODO: warn user no fuel statistics
            }

            if (fuelRate != null){
                fuelRate.setDecimalPoints(2);
                if (fuelType != -1) {
                    fuelRate.setFuelType(fuelType);
                }
                if(speed != null) {
                    calculatedMpg = new CalculatedMpg(speed, fuelRate);
                }
                else {
                    // TODO: warn user no MPG
                }
            }

            currentTimestamp = new CurrentTimestamp();
            randomGenerator = new RandomGenerator(-30, 30);

            poller.addPollingElement(engineSpeed);
            poller.addPollingElement(speed);
            poller.addPollingElement(randomGenerator);

            poller.addPollCompleteListener(currentTimestamp);
            poller.addPollCompleteListener(calculatedMpg);

            try {
                poller.addPollCompleteListener((PollCompleteListener) fuelRate);
            }
            catch (ClassCastException e) {}

            poller.start();

            if (fuelType == -1 || (engineCapacity == -1 && engineCapacityRequired)) {
                ((MainActivity) context).alertUser(UserAlert.VEHICLE_SPEC_UNKNOWN);
            }

            currentLocation = new CurrentLocation(context);
            calculatedInclination = new CalculatedInclination();
            currentLocation.addDataInputListener(calculatedInclination);

            initialised = true;
        }
    }

    private void getVehicleSpecs(SharedPreferences sharedPreferences) {

        try {
            fuelType = Integer.parseInt( sharedPreferences.getString(FUELTYPE_PREFERENCE, "-1") );
        }
        catch (NumberFormatException e) {}

        try {
            engineCapacity = Integer.parseInt( sharedPreferences.getString(ENGINESIZE_PREFERENCE, "-1") );
        }
        catch (NumberFormatException e) {}

        if (fuelType == -1) {
            // try and get the fuel type from obd
            Parameter fuelTypeParam = obd.getPid("51");
            if (fuelTypeParam != null) {
                fuelType = fuelTypeParam.getData()[0];
            }
        }
    }


    private DataSource<Double> getMassAirFlowSource(Poller poller) {

        Parameter maf = obd.getPid("10");
        if (maf != null) {
            VehicleDataLogger massAirFlow = new VehicleDataLogger(
                    maf,
                    "MAF",
                    "g/s",
                    256,
                    100,
                    2
            );
            engineCapacityRequired = false;
            poller.addPollingElement(massAirFlow);
            return massAirFlow;
        }

        // this is an alternative MAF sensor
        Parameter mafSensor = obd.getPid("66");
        if (mafSensor != null) {
            VehicleDataLogger massAirFlow = new MassAirFlowSensor(
                    mafSensor,
                    "MAF",
                    "g/s",
                    256,
                    32,
                    5
            );
            engineCapacityRequired = false;
            poller.addPollingElement(massAirFlow);
            return massAirFlow;
        }

        // if no MAF sensors then do it the hard way
        Parameter manifoldPressure = obd.getPid("0B");
        Parameter intakeTemperature = obd.getPid("0F");

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

            calculatedMaf.setEngineSpecs(this);

            poller.addPollingElement(manifoldAbsolutePressure);
            poller.addPollingElement(intakeAirTemperature);
            poller.addPollCompleteListener(calculatedMaf);

            engineCapacityRequired = true;
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

    public DataSource<Location> getCurrentLocation() {
        return currentLocation;
    }

    public DataSource<Double> getCalculatedInclination() {
        return calculatedInclination;
    }

    public DataSource<Double> getRandomGenerator() {
        return randomGenerator;
    }

    public static boolean isEngineCapacityRequired() {
        return engineCapacityRequired;
    }

    @Override
    public int getEngineCapacity() {
        return engineCapacity == -1? DEFAULT_DISPLACEMENT : engineCapacity;
    }

    @Override
    public int getFuelType() {
        return fuelType == -1? DEFAULT_FUELTYPE : fuelType;
    }
}
