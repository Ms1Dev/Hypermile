package com.example.hypermile.dataGathering.sources;

import android.util.Log;

import com.example.hypermile.obd.Parameter;

public class MassAirFlowSensor extends VehicleDataLogger {
    public MassAirFlowSensor(Parameter parameter, String name, String units, double upperByteMultiplier, int divisor, int expectedBytes) {
        super(parameter, name, units, upperByteMultiplier, divisor, expectedBytes);
    }

    public void processResponse(byte[] data) {
        try {
            int shiftValue = 2 * data[0];

            int upperByte = (data[1 + shiftValue] & 0xFF);
            int lowerByte = (data[2 + shiftValue] & 0xFF);

            double value = (upperByte * upperByteMultiplier + lowerByte) / divisor;

            notifyObservers(value);
        }
        catch (IndexOutOfBoundsException e) {
            Log.e("Err", "processResponse: Incorrect bytes given", e);
        }
    }
}
