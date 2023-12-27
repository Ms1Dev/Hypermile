package com.example.hypermile.dataGathering;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.example.hypermile.MainActivity;
import com.example.hypermile.UserAlert;
import com.example.hypermile.api.VinDecode;
import com.example.hypermile.dataGathering.sources.CalculatedMaf;
import com.example.hypermile.dataGathering.sources.CurrentTimestamp;
import com.example.hypermile.dataGathering.sources.CalculatedFuelRate;
import com.example.hypermile.dataGathering.sources.CalculatedMpg;
import com.example.hypermile.dataGathering.sources.MassAirFlowSensor;
import com.example.hypermile.dataGathering.sources.VehicleDataLogger;
import com.example.hypermile.obd.Obd;
import com.example.hypermile.obd.Parameter;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;

public class DataManager {
    private static final String PREFERENCE_FILENAME = "Hypermile_preferences";
    private static final String FUELTYPE_PREFERENCE = "fuelType";
    private static final String ENGINESIZE_PREFERENCE = "engineSize";
    private CurrentTimestamp currentTimestamp;
    private VehicleDataLogger engineSpeed;
    private VehicleDataLogger speed;
    private DataSource<Double> massAirFlow;
    private DataSource<Double> fuelRate;
    private CalculatedMpg calculatedMpg;
    private String fuelType;
    private String engineCapacity;
    private JSONObject vehicleDetails;
    private static DataManager instance;
    private Context context;
    private boolean initialised = false;
    private boolean engineCapacityRequired = true;
    private DataManager(){};
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void initialise(Context context) {
        if (!initialised) {
            this.context = context.getApplicationContext();

            getVehicleSpecs(Obd.getVin());

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

            if (fuelType == null || (engineCapacity == null && engineCapacityRequired)) {
                ((MainActivity) context).alertUser(UserAlert.VEHICLE_SPEC_UNKNOWN);
            }

            initialised = true;
        }
    }

    private void getVehicleSpecs(String vin) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        fuelType = sharedPreferences.getString(FUELTYPE_PREFERENCE, null);
        engineCapacity = sharedPreferences.getString(ENGINESIZE_PREFERENCE, null);

        if (fuelType == null) {
            // try and get the fuel type from obd
            Parameter fuelTypeParam = Obd.getPid("51");
            if (fuelTypeParam != null) {
                fuelType = String.valueOf(fuelTypeParam.getData()[0]);
            }
        }

        vehicleDetails = getVehicleDetailsJSON("1FAFP53UX4A162757");

        if ( fuelType == null || (engineCapacity == null && ( !(Obd.supportsPid("10") || Obd.supportsPid("66")) ) ) ) {
            try {
                JSONObject engineDetails = vehicleDetails.getJSONObject("engine");
                engineCapacity = String.valueOf(engineDetails.getInt("displacement"));
                fuelType = translateFuelType(engineDetails.getString("type"));
            }
            catch (JSONException e) {}

            sharedPreferences.edit()
                    .putString(ENGINESIZE_PREFERENCE, String.valueOf(engineCapacity))
                    .putString(FUELTYPE_PREFERENCE, String.valueOf(fuelType))
                    .apply();
        }
    }

    private String translateFuelType(String fuelType) {
        switch (fuelType) {
            case "gas":
            case "petrol":
                return "petrol";
            case "diesel":
                return "diesel";
        }
        return null;
    }

    private JSONObject getVehicleDetailsJSON(String vin) {
        JSONObject vehicleDetails;
        String rootPath = context.getFilesDir().getPath();
        String filepath = rootPath + "/" + vin;
        File file = new File(filepath);
        if (file.exists()) {
            try {
                vehicleDetails = readJsonFromFile(filepath);
            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            VinDecode vinDecode = new VinDecode(vin);
            vehicleDetails = vinDecode.getResponse();
            try {
                writeJsonToFile(filepath, vehicleDetails);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return vehicleDetails;
    }


    public static void writeJsonToFile(String filename, JSONObject jsonObject) throws IOException {
        String stringifyJson = jsonObject.toString();
        FileWriter fileWriter = new FileWriter(filename);
        fileWriter.write(stringifyJson);
        fileWriter.close();
    }

    public static JSONObject readJsonFromFile(String filename) throws IOException, JSONException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();
        return new JSONObject(stringBuilder.toString());
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
            engineCapacityRequired = false;
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
            engineCapacityRequired = false;
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

            if (engineCapacity != null) {
                calculatedMaf.setEngineDisplacementCC(Integer.parseInt(engineCapacity));
            }

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

    public boolean isEngineCapacityRequired() {
        return engineCapacityRequired;
    }
}
