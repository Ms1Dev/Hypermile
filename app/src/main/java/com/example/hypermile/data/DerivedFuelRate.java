package com.example.hypermile.data;

public class DerivedFuelRate extends DataSource implements DataInputObserver {
    final static private double STOICHIOMETRIC_PETROL_E10 = 14.1;
    final static private double STOICHIOMETRIC_PETROL = 14.7;
    final static private double DENSITY_PETROL_GRAM_LITRE = 750;
    int fuelType = 1; // 1: petrol 4: diesel

    public DerivedFuelRate(VehicleDataLogger massAirFlow) {
        massAirFlow.addDataInputListener(this);
    }

    @Override
    public void incomingData(double data) {
        double fuelRate = data * 3600 / STOICHIOMETRIC_PETROL_E10 / DENSITY_PETROL_GRAM_LITRE;
        notifyObservers(fuelRate);
    }

    @Override
    public void setUnits(String units) {
        this.units = "L/h";
    }
}
