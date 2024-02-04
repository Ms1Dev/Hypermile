package com.example.hypermile.dataGathering.sources;



import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;
import com.example.hypermile.dataGathering.EngineSpec;
import com.example.hypermile.dataGathering.PollCompleteListener;

/**
 * Calculates the mass airflow using Intake pressure, Intake temperature, RPM, Engine capacity.
 * This is in the event the vehicle does not have a dedicated mass airflow sensor.
 * It is less accurate than a MAF sensor and uses an estimated volumetric efficiency as the actual efficiency is unknown.
 */
public class CalculatedMaf extends DataSource<Double> implements PollCompleteListener {
    final static double UNIVERSAL_GAS_CONSTANT = 82.1; // (cm3 •atm)/(mole•K)
    final static double MASS_OF_AIR = 28.949; // g
    final static double KPA_ATM_CONVERSION = 0.00986923;
    final static double VOLUMETRIC_EFFICIENCY = 0.8;
    private EngineSpec engineSpec;
    double manifoldAbsolutePressure;
    double intakeTemperature;
    double engineSpeed;
    boolean hasManifoldAbsolutePressure;
    boolean hasIntakeTemperature;
    boolean hasEngineSpeed;


    public CalculatedMaf(VehicleDataLogger manifoldAbsolutePressure, VehicleDataLogger intakeTemperature, VehicleDataLogger engineSpeed) {
        manifoldAbsolutePressure.addDataInputListener( new DataInputObserver<Double>() {
            @Override
            public void incomingData(Double data) {
                newManifoldAbsolutePressureData(data);
            }
        });

        intakeTemperature.addDataInputListener( new DataInputObserver<Double>() {
            @Override
            public void incomingData(Double data) {
                newIntakeTemperatureData(data);
            }
        });

        engineSpeed.addDataInputListener( new DataInputObserver<Double>() {
            @Override
            public void incomingData(Double data) {
                newEngineSpeedData(data);
            }
        });
    }

    @Override
    public String getName() {
        return "MAF";
    }

    public void setEngineSpecs(EngineSpec engineSpec) {
        this.engineSpec = engineSpec;
    }

    /**
     * Called when new intake pressure data received
     * @param data
     */
    public void newManifoldAbsolutePressureData(double data) {
        manifoldAbsolutePressure = data;
        hasManifoldAbsolutePressure = true;
        calculateData();
    }

    /**
     * Called when new intake temperature data received
     * @param data
     */
    public void newIntakeTemperatureData(double data) {
        intakeTemperature = data;
        hasIntakeTemperature = true;
        calculateData();
    }

    /**
     * Called when new RPM data is received
     * @param data
     */
    public void newEngineSpeedData(double data) {
        engineSpeed = data;
        hasEngineSpeed = true;
        calculateData();
    }

    /**
     * Calculates the mass flow in cubic centimetres per second
     * @param absoluteIntakePressure
     * @param rpm
     * @param temperature
     * @return cc/s
     */
    public double calculateMassFlow(double absoluteIntakePressure, double rpm, double temperature) {

        double intakePressure = kPaToAtm(absoluteIntakePressure);

        double gasDensity = gasDensity( intakePressure, (int) (temperature + 0.5) );

        double intakeVolumePerMinute = rpm * ( engineSpec.getEngineCapacity() * VOLUMETRIC_EFFICIENCY / 2.0 );

        return  intakeVolumePerMinute * gasDensity / 60;
    }

    /**
     * Calculates the density of air based on pressure and temperature
     * Equation taken from: https://www.first-sensor.com/cms/upload/appnotes/AN_Massflow_E_11153.pdf
     * @param pressure
     * @param temperature
     * @return air density
     */
    public static double gasDensity(double pressure, int temperature) {
        return (MASS_OF_AIR * pressure) / (UNIVERSAL_GAS_CONSTANT * celsiusToKelvin(temperature));
    }

    /**
     * Converts celsius to kelvin
     * @param celsius
     * @return kelvin
     */
    public static double celsiusToKelvin(int celsius) {
        return celsius + 273.15;
    }

    /**
     * Pressure conversion from kPa to atmospheric
     * @param kPa
     * @return atm
     */
    public static double kPaToAtm(double kPa) {
        return kPa * KPA_ATM_CONVERSION;
    }

    /**
     * Calculates new data if all required data has been received
     */
    private void calculateData() {
        if (hasManifoldAbsolutePressure && hasIntakeTemperature && hasEngineSpeed) {
            notifyObservers(calculateMassFlow(manifoldAbsolutePressure, engineSpeed, intakeTemperature));
        }
    }

    /**
     * Resets the data flags to invalidate old data
     */
    @Override
    public void pollingComplete() {
        hasManifoldAbsolutePressure = false;
        hasIntakeTemperature = false;
        hasEngineSpeed = false;
    }
}
