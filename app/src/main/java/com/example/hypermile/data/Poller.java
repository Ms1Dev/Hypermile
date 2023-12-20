package com.example.hypermile.data;

import android.util.Log;

import com.example.hypermile.bluetoothDevices.Connection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Poller extends Thread {
    private int sleepDuration = 500;

    ArrayList<VehicleDataLogger> vehicleDataPoints = new ArrayList<>();

    public void addVehicleDataPoint(VehicleDataLogger vehicleData) {
        vehicleDataPoints.add(vehicleData);
    }

    public Poller(int sampleRateHz) {
        sleepDuration = 1000 / sampleRateHz;
    }

//    public void run() {
//        Connection connection = Connection.getInstance();
//        while(true) {
//
//            // wait for bluetooth connection
//            while (!connection.hasConnection());
//
//            while (connection.hasConnection()) {
//                try {
//                    sleep(sleepDuration);
//
//                    for (VehicleData vehicleData : this.vehicleData) {
//                        connection.send(vehicleData.requestCode());
//                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                        connection.readBuffer(outputStream);
//                        InputStream receiveStream = new ByteArrayInputStream(outputStream.toByteArray());
//                        vehicleData.passResponse(receiveStream);
//                    }
//
//                } catch (InterruptedException | IOException e) {
//                    Log.e("Err", "run: ", e);
//                }
//            }
//        }
//    }

    public void run() {
        Random random = new Random(System.currentTimeMillis());
        while(true) {
            try {
                sleep(sleepDuration);

                for (VehicleDataLogger vehicleData : this.vehicleDataPoints) {

                    String req = new String(vehicleData.requestCode());

                    byte[] data = new byte[5];
                    int bound;
                    int randomVal;

                    switch (req) {
                        case "010C\r":
                            bound = 7000;
                            randomVal = random.nextInt(bound);

                            randomVal *= 4;

                            data[0] = (byte) (randomVal / 256);
                            data[1] = (byte) randomVal;

                            break;
                        case "0110\r":
                            bound = 200;
                            randomVal = random.nextInt(bound);
                            randomVal *= 100;

                            int upperval = (int) ((randomVal / 256.0) + 0.5);

                            data[0] = (byte) upperval;
                            data[1] = (byte) randomVal;

                            double value = (data[0] * 256.0 + data[1]) / 100.0;
                            break;
                    }

                    vehicleData.processResponse(data);
                }

            } catch (InterruptedException e) {
                Log.e("Err", "run: ", e);
            }
        }

    }
}
