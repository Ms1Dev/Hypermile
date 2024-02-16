package com.example.hypermile.dataGathering.sources;


import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;
import com.example.hypermile.dataGathering.PollCompleteListener;

/**
 * Calculates the MPG from speed and fuel rate.
 * This class behaves like a virtual data source. It listens for data changes on two other
 * data sources, processes the data, and then forwards the result on to any observers
 */
public class CalculatedMpg extends DataSource<Double> implements PollCompleteListener {
    final static private double UK_GALLON_CONVERSION = 0.21996923465436;
    final static private double MAX_MPG = 99.99;
    final static private double KPH_MPH_CONVERSION = 0.621371;
    private boolean newSpeedData = false;
    private boolean newFuelData = false;
    private double milesPerHour;
    private double litresPerHour;
    private VehicleDataLogger speed;


    public CalculatedMpg(VehicleDataLogger speed, DataSource<Double> fuelRate) {
        /*
            Create listeners for speed and fuel rate data which is used in calculation
         */
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

    /**
     * Called by the speed data observer when new data available
     * @param data
     */
    public void newSpeedData(double data) {
        milesPerHour = data * KPH_MPH_CONVERSION;
        newSpeedData = true;
        calculateData();
    }

    /**
     * Called by the fuelrate data observer when new data available
     * @param data
     */
    public void newFuelData(double data) {
        litresPerHour = data;
        newFuelData = true;
        calculateData();
    }

    /**
     * If there is new data for fuel rate and speed when this is called then it will calculate
     * MPG and notify observers of the result
     */
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

    /**
     * It is possible that data is only received from one source per round of polling.
     * To prevent this old data being used in the next cycle the new data flags are set to false
     * at the end of each round of polling to make sure they are not used by mistake.
     */
    @Override
    public void pollingComplete() {
        newFuelData = false;
        newSpeedData = false;
    }
}
