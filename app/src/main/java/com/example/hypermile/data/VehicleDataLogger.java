package com.example.hypermile.data;

import android.util.Log;

import com.example.hypermile.bluetoothDevices.Connection;
import com.example.hypermile.obd.ObdFrame;


public class VehicleDataLogger extends DataSource implements PollingElement {
    private final static int RESPONSE_DELAY = 100;
    String name;
    String code;
    double upperByteMultiplier;
    double divisor;
    int expectedBytes;
    Connection connection;

    public VehicleDataLogger(String name, String units, String code, double upperByteMultiplier, int divisor, int expectedBytes) {
        this.units = units;
        this.name = name;
        this.code = code;
        this.upperByteMultiplier = upperByteMultiplier;
        this.divisor = divisor;
        this.expectedBytes = expectedBytes;
        connection = Connection.getInstance();
    }

    public byte[] requestCode() {
        return code.getBytes();
    }

    public void processResponse(byte[] data) {
        try {
            int upperByte = data[0] & 0xFF;
            int lowerByte = 0;
            if (expectedBytes == 2) {
                lowerByte = data[1] & 0xFF;
            }

            double value = (upperByte * upperByteMultiplier + lowerByte) / divisor;

            notifyObservers(value);
        }
        catch (IndexOutOfBoundsException e) {
            Log.e("Err", "processResponse: Incorrect bytes given", e);
        }
    }

    private byte[] requestObdData() {
        connection.send(requestCode());

        try {
            Thread.sleep(RESPONSE_DELAY);
        } catch (InterruptedException e) {
            return null;
        }

        ObdFrame obdFrame = connection.getLatestFrame();

        if (obdFrame != null) {
            return obdFrame.getPayload();
        }
        else {
            Log.d("TAG", "run: MISS");
        }

        return null;
    }

    @Override
    public void sampleData() {
        byte[] vehicleData = requestObdData();
        if (vehicleData != null) {
            processResponse(vehicleData);
        }
    }
}
