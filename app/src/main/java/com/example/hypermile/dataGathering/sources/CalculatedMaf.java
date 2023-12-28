package com.example.hypermile.dataGathering.sources;


import android.util.Log;

import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataSource;
import com.example.hypermile.dataGathering.PollCompleteListener;

public class CalculatedMaf extends DataSource<Double> implements PollCompleteListener {
    final static double UNIVERSAL_GAS_CONSTANT = 82.1; // (cm3 •atm)/(mole•K)
    final static double MASS_OF_AIR = 28.949; // g
    final static double KPA_ATM_CONVERSION = 0.00986923;
    final static double VOLUMETRIC_EFFICIENCY = 0.9;
    int engineDisplacementCC = 1600;
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

    public void setEngineDisplacementCC(int engineDisplacementCC) {
        this.engineDisplacementCC = engineDisplacementCC;
    }

    public void newManifoldAbsolutePressureData(double data) {
        manifoldAbsolutePressure = data;
        hasManifoldAbsolutePressure = true;
        calculateData();
    }

    public void newIntakeTemperatureData(double data) {
        intakeTemperature = data;
        hasIntakeTemperature = true;
        calculateData();
    }

    public void newEngineSpeedData(double data) {
        engineSpeed = data;
        hasEngineSpeed = true;
        calculateData();
    }

    /**
     * @brief Calculates the mass flow in cubic centimetres per second
     * @param absoluteIntakePressure
     * @param rpm
     * @param temperature
     * @return cc/s
     */
    public double calculateMassFlow(double absoluteIntakePressure, double rpm, double temperature) {

        double intakePressure = kPaToAtm(absoluteIntakePressure);

        double gasDensity = gasDensity( intakePressure, (int) (temperature + 0.5) );

        double intakeVolumePerMinute = rpm * ( engineDisplacementCC / 2.0 );

        return  intakeVolumePerMinute * gasDensity * VOLUMETRIC_EFFICIENCY / 60;
    }

    /**
     * @brief Calculates the density of air based on pressure and temperature
     * @param pressure
     * @param temperature
     * @return air density
     */
    public static double gasDensity(double pressure, int temperature) {
        // source: https://www.first-sensor.com/cms/upload/appnotes/AN_Massflow_E_11153.pdf
        return (MASS_OF_AIR * pressure) / (UNIVERSAL_GAS_CONSTANT * celsiusToKelvin(temperature));
    }

    public static double celsiusToKelvin(int celsius) {
        return celsius + 273.15;
    }

    public static double kPaToAtm(double kPa) {
        return kPa * KPA_ATM_CONVERSION;
    }

    private void calculateData() {
        if (hasManifoldAbsolutePressure && hasIntakeTemperature && hasEngineSpeed) {
            notifyObservers(calculateMassFlow(manifoldAbsolutePressure, engineSpeed, intakeTemperature));
        }
    }

    /**
     * @brief Resets the data flags to invalidate old data
     */
    @Override
    public void pollingComplete() {
        hasManifoldAbsolutePressure = false;
        hasIntakeTemperature = false;
        hasEngineSpeed = false;
    }
}
