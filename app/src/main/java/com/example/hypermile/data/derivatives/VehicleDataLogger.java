package com.example.hypermile.data.derivatives;

import android.util.Log;

import com.example.hypermile.bluetoothDevices.Connection;
import com.example.hypermile.data.DataSource;
import com.example.hypermile.data.PollingElement;
import com.example.hypermile.obd.Obd;
import com.example.hypermile.obd.ObdFrame;
import com.example.hypermile.obd.Pid;


public class VehicleDataLogger extends DataSource<Double> implements PollingElement {
    String name;
    String code;
    double upperByteMultiplier;
    double divisor;
    int expectedBytes;
    int offset = 0;
    Pid pid;

    public VehicleDataLogger(Pid pid, String name, String units, int offset) {
        this.units = units;
        this.name = name;
        this.pid = pid;
        this.upperByteMultiplier = 1;
        this.divisor = 1;
        this.expectedBytes = 1;
        this.offset = offset;
    }

    public VehicleDataLogger(Pid pid, String name, String units) {
        this.units = units;
        this.name = name;
        this.pid = pid;
        this.upperByteMultiplier = 1;
        this.divisor = 1;
        this.expectedBytes = 1;
    }

    public VehicleDataLogger(Pid pid, String name, String units, double upperByteMultiplier, int divisor, int expectedBytes) {
        this.units = units;
        this.name = name;
        this.pid = pid;
        this.upperByteMultiplier = upperByteMultiplier;
        this.divisor = divisor;
        this.expectedBytes = expectedBytes;
    }

    public byte[] requestCode() {
        return pid.getRequestCode();
    }

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
            Log.e("Err", "processResponse: Incorrect bytes given", e);
        }
    }

//    private byte[] requestObdData() {
//        connection.send(requestCode());
//
//        long currentMillis = System.currentTimeMillis();
//
//        while(!connection.hasData() && currentMillis + MAXIMUM_RESPONSE_WAIT > System.currentTimeMillis());
//
//        ObdFrame obdFrame = connection.getLatestFrame();
//
//        while(currentMillis + MINIMUM_RESPONSE_WAIT > System.currentTimeMillis());
//
//        if (obdFrame != null) {
//            return obdFrame.getPayload();
//        }
//        else {
//            Log.d("TAG", "run: MISS");
//        }
//
//        return null;
//    }

    @Override
    public void sampleData() {
        byte[] vehicleData = pid.getData();
        if (vehicleData != null) {
            processResponse(vehicleData);
        }
    }
}
