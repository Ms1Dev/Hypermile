package com.example.hypermile.dataGathering.sources;

import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;

public class CalculatedFuelRate extends DataSource<Double> implements DataInputObserver<Double> {
    final static private double STOICHIOMETRIC_PETROL = 14.7;
    final static private double STOICHIOMETRIC_DIESEL = 14.5;
    final static private double DENSITY_PETROL_GRAM_LITRE = 750;
    int fuelType = 1; // 1: petrol 4: diesel
    double airFuelRatio = STOICHIOMETRIC_PETROL;
    DataSource<Double> massAirFlow;

    public CalculatedFuelRate(DataSource<Double> massAirFlow) {
        this.massAirFlow = massAirFlow;
        massAirFlow.addDataInputListener(this);
        units = "L/h";
    }

    @Override
    public void incomingData(Double data) {
        double fuelRate = data * 3600 / airFuelRatio / DENSITY_PETROL_GRAM_LITRE;
        notifyObservers(fuelRate);
    }

    public void setFuelType(int fuelType) {
        this.fuelType = fuelType;
        airFuelRatio = fuelType == 4? STOICHIOMETRIC_DIESEL : STOICHIOMETRIC_PETROL;
    }

    @Override
    public String getName() {
        return "Fuel Rate";
    }
}
