package com.example.hypermile;


import com.example.hypermile.data.DataInputObserver;
import com.example.hypermile.data.DataSource;
import com.example.hypermile.data.Poller;
import com.example.hypermile.data.VehicleDataLogger;

public class DataManager {
    private static DataManager instance;
    private boolean initialised = false;

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void initialise() {
        if (!initialised) {
            createVehicleDataLoggers();

            Poller poller = new Poller(1);
            poller.addPollingElement(engineSpeed);
            poller.addPollingElement(massAirFlow);
            poller.addPollingElement(speed);
            poller.start();

            derivedFuelRate = new DerivedFuelRate(massAirFlow);
            derivedMpg = new DerivedMpg(speed, derivedFuelRate);

            initialised = true;
        }
    }

    public boolean isInitialised() {
        return initialised;
    }

    private VehicleDataLogger engineSpeed;
    private VehicleDataLogger massAirFlow;
    private VehicleDataLogger speed;
    private DerivedMpg derivedMpg;
    private DerivedFuelRate derivedFuelRate;

    private DataManager(){};


    private void createVehicleDataLoggers() {
        engineSpeed = new VehicleDataLogger(
                "Engine Speed",
                "RPM",
                "010C\r",
                256,
                4,
                2
        );
        massAirFlow = new VehicleDataLogger(
                "MAF",
                "g/s",
                "0110\r",
                256,
                100,
                2
        );
        speed = new VehicleDataLogger(
                "Speed",
                "MPH",
                "010D\r",
                1,
                1,
                1
        );
    }

    public VehicleDataLogger getEngineSpeed() {
        return engineSpeed;
    }

    public VehicleDataLogger getMassAirFlow() {
        return massAirFlow;
    }

    public VehicleDataLogger getSpeed() {
        return speed;
    }

    public DerivedMpg getDerivedMpg() {
        return derivedMpg;
    }

    public DerivedFuelRate getDerivedFuelRate() {
        return derivedFuelRate;
    }

    public class DerivedMpg extends DataSource<Double> {
        final static private double UK_GALLON_CONVERSION = 0.21996923465436;
        final static private double MAX_MPG = 99.99;
        boolean newSpeedData = false;
        boolean newFuelData = false;
        double milesPerHour;
        double litresPerHour;

        public DerivedMpg(VehicleDataLogger speed, DerivedFuelRate fuelRate) {
            speed.addDataInputListener( new DataInputObserver<Double>() {
                @Override
                public void incomingData(Double data) {
                    newSpeedData(data);
                }

                @Override
                public void setUnits(String units) {}
            });

            fuelRate.addDataInputListener(new DataInputObserver<Double>() {
                @Override
                public void incomingData(Double data) {
                    newFuelData(data);
                }

                @Override
                public void setUnits(String units) {}
            });
        }

        public void newSpeedData(double data) {
            milesPerHour = data;
            newSpeedData = true;
            calculateData();
        }

        public void newFuelData(double data) {
            litresPerHour = data;
            newFuelData = true;
            calculateData();
        }

        private void calculateData() {
            if (newFuelData && newSpeedData) {
                newFuelData = false;
                newSpeedData = false;

                double gallonsPerHour = litresPerHour * UK_GALLON_CONVERSION;
                double milesPerGallon = milesPerHour / gallonsPerHour;

                if (milesPerGallon > MAX_MPG) milesPerGallon = MAX_MPG;

                notifyObservers(milesPerGallon);
            }
        }
    }


    public class DerivedFuelRate extends DataSource<Double> implements DataInputObserver<Double> {
        final static private double STOICHIOMETRIC_PETROL_E10 = 14.1;
        final static private double STOICHIOMETRIC_PETROL = 14.7;
        final static private double DENSITY_PETROL_GRAM_LITRE = 750;
        int fuelType = 1; // 1: petrol 4: diesel

        public DerivedFuelRate(VehicleDataLogger massAirFlow) {
            massAirFlow.addDataInputListener(this);
        }

        @Override
        public void incomingData(Double data) {
            double fuelRate = data * 3600 / STOICHIOMETRIC_PETROL_E10 / DENSITY_PETROL_GRAM_LITRE;
            notifyObservers(fuelRate);
        }

        @Override
        public void setUnits(String units) {
            this.units = "L/h";
        }
    }


}
