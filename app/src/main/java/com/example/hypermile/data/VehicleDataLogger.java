package com.example.hypermile.data;

import android.util.Log;

import java.util.Map;


public class VehicleDataLogger extends DataPoint {
    String unit;
    String name;
    String code;
    double upperByteMultiplier;
    double divisor;
    int expectedBytes;

    public VehicleDataLogger(String name, String unit, String code, double upperByteMultiplier, int divisor, int expectedBytes) {
        this.unit = unit;
        this.name = name;
        this.code = code;
        this.upperByteMultiplier = upperByteMultiplier;
        this.divisor = divisor;
        this.expectedBytes = expectedBytes;
    }

    public byte[] requestCode() {
        return code.getBytes();
    }

    public void processResponse(byte[] data) {
        try {
            byte upperByte = data[0];
            byte lowerByte = 0;
            if (expectedBytes == 2) {
                lowerByte = data[1];
            }

            double value = (upperByte * upperByteMultiplier + lowerByte) / divisor;

            notifyObservers(value);
        }
        catch (IndexOutOfBoundsException e) {

        }
    }
}
