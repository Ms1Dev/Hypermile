package com.example.hypermile.data;

import android.util.Log;

import com.example.hypermile.bluetoothDevices.Connection;
import com.example.hypermile.obd.ObdFrame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Poller extends Thread {
    private final static int RESPONSE_DELAY = 100;
    private int sleepDuration = 500;

    private PollCompleteListener pollCompleteListener;


    ArrayList<PollingElement> pollingElements = new ArrayList<>();

    public void addPollingElement(PollingElement pollingElement) {
        pollingElements.add(pollingElement);
    }

    public void setPollCompleteListener(PollCompleteListener pollCompleteListener) {
        this.pollCompleteListener = pollCompleteListener;
    }

    public Poller(int sampleRateHz) {
        sleepDuration = 1000 / sampleRateHz;
    }

    public void run() {
        Connection connection = Connection.getInstance();
        while(true) {

            // wait for bluetooth connection
            while (!connection.hasConnection());

            while (connection.hasConnection()) {
                try {
                    sleep(sleepDuration);

                    for (PollingElement pollingElement : this.pollingElements) {
                        pollingElement.sampleData();
                    }

                    if (pollCompleteListener != null) {
                        pollCompleteListener.pollingComplete();
                    }

                } catch (InterruptedException e) {
                    Log.e("Err", "run: ", e);
                }
            }
        }
    }


//    public void run() {
//        Random random = new Random(System.currentTimeMillis());
//        while(true) {
//            try {
//                sleep(sleepDuration);
//
//                for (VehicleDataLogger vehicleData : this.vehicleDataPoints) {
//
//                    String req = new String(vehicleData.requestCode());
//
//                    byte[] data = new byte[5];
//                    int bound;
//                    int randomVal;
//
//                    switch (req) {
//                        case "010C\r":
//                            bound = 7000;
//                            randomVal = random.nextInt(bound);
//
//                            randomVal *= 4;
//
//                            data[0] = (byte) (randomVal / 256);
//                            data[1] = (byte) randomVal;
//
//                            break;
//                        case "010D\r":
//                            bound = 110;
//                            randomVal = random.nextInt(bound);
//
//                            data[0] = (byte) randomVal;
//                            break;
//                        case "0110\r":
//                            bound = 600;
//                            randomVal = random.nextInt(bound);
//
//                            randomVal *= 100;
//
//                            int upperval = (int) ((randomVal / 256.0) + 0.5);
//
//                            data[0] = (byte) upperval;
//                            data[1] = (byte) randomVal;
//                            break;
//                    }
//
//                    vehicleData.processResponse(data);
//                }
//
//            } catch (InterruptedException e) {
//                Log.e("Err", "run: ", e);
//            }
//        }
//
//    }
}
