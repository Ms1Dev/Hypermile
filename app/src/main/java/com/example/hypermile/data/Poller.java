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

public class Poller extends Thread {
    private int sleepDuration = 500;

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

                    byte[] request = "010C\r".getBytes();

                    byte[] responseBuffer = new byte[1024];

                    connection.send(request);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    connection.readBuffer(outputStream);
                    InputStream receiveStream = new ByteArrayInputStream(outputStream.toByteArray());

                    receiveStream.read(responseBuffer);
                    receiveStream.reset();
                    receiveStream.close();

                    String response = new String(responseBuffer, StandardCharsets.UTF_8);

                    Log.d("Res", response);
                } catch (InterruptedException | IOException e) {
                    Log.e("Err", "run: ", e);
                }
            }
        }
    }
}
