package com.example.hypermile.dataGathering;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import androidx.preference.PreferenceManager;

import com.example.hypermile.MainActivity;
import com.example.hypermile.UserAlert;
import com.example.hypermile.bluetooth.ConnectionEventListener;
import com.example.hypermile.bluetooth.ConnectionState;
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
import java.util.ArrayList;

/**
 * This class acts like a central access point for all data sources. In more detail it does the following:
 * - Creates an instance of Poller, which requests data from data sources at regular intervals
 * - Initialises all data sources and connects them to the polling object.
 * - Provides getters for accessing data sources.
 * - If required, it will also try to load vehicle details from shared preferences
 */
public class DataManager implements EngineSpec, ConnectionEventListener {
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
    private Obd obd;
    private static boolean engineCapacityRequired = true;
    ArrayList<DataManagerReadyListener> dataManagerReadyListeners = new ArrayList<>();

    public void addDataManagerReadyListener(DataManagerReadyListener dataManagerReadyListener) {
        dataManagerReadyListeners.add(dataManagerReadyListener);
    }

    protected void notifyListeners() {
        for (DataManagerReadyListener dataManagerReadyListener : dataManagerReadyListeners) {
            dataManagerReadyListener.dataManagerReady();
        }
    }

    public DataManager(Context context, Obd obd){
        this.context = context;
        this.obd = obd;
    };

    /**
     * Create instances of various data sources.
     * Also creates a polling object that will request an update from all data sources at regular intervals.
     */
    public void initialise() {
        if (!initialised) {
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


            /*
                see ./sources/VehicleDataLogger.java class for more info on the vehicle data logger and its parameters
             */
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

            // the mass air flow data can come from various sources depending on what sensors the vehicle has
            massAirFlow = getMassAirFlowSource(poller);

            // fuel rate is calculated from mass air flow
            if (massAirFlow != null) {
                fuelRate = new CalculatedFuelRate(massAirFlow);
                massAirFlow.setDecimalPoints(2);
            }

            if (fuelRate != null){
                fuelRate.setDecimalPoints(2);
                if (fuelType != -1) {
                    fuelRate.setFuelType(fuelType);
                }
                if(speed != null) {
                    calculatedMpg = new CalculatedMpg(speed, fuelRate);
                }
            }

            currentTimestamp = new CurrentTimestamp();
            randomGenerator = new RandomGenerator(-30, 30);

            // elements that sample their data during polling
            poller.addPollingElement(engineSpeed);
            poller.addPollingElement(speed);
            poller.addPollingElement(randomGenerator);

            // these elements are notified when a round of polling has completed
            poller.addPollCompleteListener(currentTimestamp);
            poller.addPollCompleteListener(calculatedMpg);

            try {
                poller.addPollCompleteListener((PollCompleteListener) fuelRate);
            }
            catch (ClassCastException e) {}

            poller.start();

            // If the alternate MAF calculation is used the engine capacity and fuel type are needed so alert user.
            if (fuelType == -1 || (engineCapacity == -1 && engineCapacityRequired)) {
                ((MainActivity) context).alertUser(UserAlert.VEHICLE_SPEC_UNKNOWN);
            }

            currentLocation = new CurrentLocation(context);
            calculatedInclination = new CalculatedInclination();
            currentLocation.addDataInputListener(calculatedInclination);

            initialised = true;
            notifyListeners();
        }
    }

    /**
     * This will try to get fuel type and engine size from shared preferences.
     * Engine size and fuel type are need for some calculations.
     */
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

    /**
     * Mass airflow can come from 3 possible sources. Either directly from 2 mass airflow sensors
     * OR by calculating it from various other sensors (see ./sources/CalculatedMaf.java class)
     */
    private DataSource<Double> getMassAirFlowSource(Poller poller) {

        // This is the most common MAF sensor on a vehicle
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
        // MAF can be calculated with Intake temperature, intake pressure, RPM and engine capacity
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

    @Override
    public void onStateChange(ConnectionState connectionState) {
        if (connectionState == ConnectionState.CONNECTED && !initialised) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initialise();
                }
            });
        }
    }
}
