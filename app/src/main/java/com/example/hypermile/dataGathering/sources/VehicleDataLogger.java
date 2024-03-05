package com.example.hypermile.dataGathering.sources;

import android.util.Log;

import com.example.hypermile.dataGathering.DataSource;
import com.example.hypermile.dataGathering.PollingElement;
import com.example.hypermile.obd.Parameter;

/**
 * Generic data source used with OBD sensor reading.
 * OBD sensors provide data in a very similar format so this class covers most of them.
 * To better understand what is meant by upperByteMultiplier, divisor, etc., look at the "Formula" column
 * of the following Wikipedia table: https://en.wikipedia.org/wiki/OBD-II_PIDs#Service_01_-_Show_current_data
 * Data is usually passed as 2 bytes with a formula to decode them.
 */
public class VehicleDataLogger extends DataSource<Double> implements PollingElement {
    String code;
    double upperByteMultiplier;
    double divisor;
    int expectedBytes;
    int offset = 0;
    String name;
    Parameter parameter;

    public VehicleDataLogger(Parameter parameter, String name, String units, int offset) {
        this.units = units;
        this.name = name;
        this.parameter = parameter;
        this.upperByteMultiplier = 1;
        this.divisor = 1;
        this.expectedBytes = 1;
        this.offset = offset;
    }

    public VehicleDataLogger(Parameter parameter, String name, String units) {
        this.units = units;
        this.name = name;
        this.parameter = parameter;
        this.upperByteMultiplier = 1;
        this.divisor = 1;
        this.expectedBytes = 1;
    }

    public VehicleDataLogger(Parameter parameter, String name, String units, double upperByteMultiplier, int divisor, int expectedBytes) {
        this.units = units;
        this.name = name;
        this.parameter = parameter;
        this.upperByteMultiplier = upperByteMultiplier;
        this.divisor = divisor;
        this.expectedBytes = expectedBytes;
    }

    public byte[] requestCode() {
        return parameter.getRequestCode();
    }

    /**
     * Decode the bytes that were sent by the OBD device into a usable format
     */
    public void processResponse(byte[] data) {
        try {
            int upperByte = data[0] & 0xFF;
            int lowerByte = 0;
            if (expectedBytes == 2) {
                lowerByte = data[1] & 0xFF;
            }

            double value = (upperByte * upperByteMultiplier + lowerByte) / divisor + offset;

            notifyObservers(value);
        }
        catch (IndexOutOfBoundsException e) {
            Log.e("Err", "processResponse: Incorrect bytes given - " + name, e);
        }
    }

    @Override
    public void sampleData() {
        byte[] vehicleData = parameter.getData();
        if (vehicleData != null) {
            processResponse(vehicleData);
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
