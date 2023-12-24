package com.example.hypermile.data.derivatives;

import android.util.Log;

import com.example.hypermile.bluetoothDevices.Connection;
import com.example.hypermile.data.DataSource;
import com.example.hypermile.data.PollingElement;
import com.example.hypermile.obd.Obd;
import com.example.hypermile.obd.ObdFrame;


public class VehicleDataLogger extends DataSource<Double> implements PollingElement {
    private final static int MINIMUM_RESPONSE_WAIT = 150;
    private final static int MAXIMUM_RESPONSE_WAIT = 500;
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
        if (!Obd.isReady()) return;
        byte[] vehicleData = Obd.requestObdData(requestCode(), connection);
        if (vehicleData != null) {
            processResponse(vehicleData);
        }
    }
}
