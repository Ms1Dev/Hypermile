package com.example.hypermile.dataGathering.sources;

import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;

public class CalculatedFuelRate extends DataSource<Double> implements DataInputObserver<Double> {
    final static private double STOICHIOMETRIC_PETROL_E10 = 14.1;
    final static private double STOICHIOMETRIC_PETROL = 14.7;
    final static private double DENSITY_PETROL_GRAM_LITRE = 750;
    int fuelType = 1; // 1: petrol 4: diesel
    DataSource<Double> massAirFlow;

    public CalculatedFuelRate(DataSource<Double> massAirFlow) {
        this.massAirFlow = massAirFlow;
        massAirFlow.addDataInputListener(this);
    }

    @Override
    public void incomingData(Double data) {
        double fuelRate = data * 3600 / STOICHIOMETRIC_PETROL / DENSITY_PETROL_GRAM_LITRE;
        notifyObservers(fuelRate);
    }

    public void setUnits(String units) {
        this.units = "L/h";
    }

}
