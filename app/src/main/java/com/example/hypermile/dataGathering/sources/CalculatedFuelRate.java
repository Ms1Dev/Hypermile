package com.example.hypermile.dataGathering.sources;

import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;

/**
 * Datasource that observes the mass airflow datasource and calculates fuel rate using air-fuel ratio
 */
public class CalculatedFuelRate extends DataSource<Double> implements DataInputObserver<Double> {
    final static private double STOICHIOMETRIC_PETROL = 14.7;
    final static private double STOICHIOMETRIC_DIESEL = 14.5;
    final static private double DENSITY_PETROL_GRAM_LITRE = 750;
    final static private double DENSITY_DIESEL_GRAM_LITRE = 850;
    int fuelType = 1; // 1: petrol 4: diesel
    double airFuelRatio = STOICHIOMETRIC_PETROL;
    double density = DENSITY_PETROL_GRAM_LITRE;
    DataSource<Double> massAirFlow;

    public CalculatedFuelRate(DataSource<Double> massAirFlow) {
        this.massAirFlow = massAirFlow;
        massAirFlow.addDataInputListener(this);
        units = "L/h";
    }

    @Override
    public void incomingData(Double data) {
        double fuelRate = data * 3600 / airFuelRatio / density;
        notifyObservers(fuelRate);
    }

    /**
     * This sets the correct conversions depending on the fuel type
     * @param fuelType
     */
    public void setFuelType(int fuelType) {
        this.fuelType = fuelType;
        if (fuelType == 4) {
            airFuelRatio = STOICHIOMETRIC_DIESEL;
            density = DENSITY_DIESEL_GRAM_LITRE;
        }
        else {
            airFuelRatio = STOICHIOMETRIC_PETROL;
            density = DENSITY_PETROL_GRAM_LITRE;
        }
    }

    @Override
    public String getName() {
        return "Fuel Rate";
    }
}
