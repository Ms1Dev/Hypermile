package com.example.hypermile.data;

import android.util.Log;

import com.example.hypermile.bluetoothDevices.Connection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Poller extends Thread {
    private int sleepDuration = 500;

    ArrayList<DataPoint> dataPoints = new ArrayList<>();

    public Poller(int sampleRateHz) {
        sleepDuration = 1000 / sampleRateHz;
        dataPoints.add(new EngineSpeed());
    }

    public void run() {
        Connection connection = Connection.getInstance();
        while(true) {

            // wait for bluetooth connection
            while (!connection.hasConnection());

            while (connection.hasConnection()) {
                try {
                    sleep(sleepDuration);

                    for (DataPoint dataPoint : dataPoints) {
                        connection.send(dataPoint.requestCode());
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        connection.readBuffer(outputStream);
                        InputStream receiveStream = new ByteArrayInputStream(outputStream.toByteArray());
                        dataPoint.passResponse(receiveStream);
                    }

                } catch (InterruptedException | IOException e) {
                    Log.e("Err", "run: ", e);
                }
            }
        }
    }
}
