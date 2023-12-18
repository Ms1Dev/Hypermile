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
        while(!Connection.hasConnection());

        Log.d("Res", "has connection");


        while(true) {
            try {
                sleep(sleepDuration);

                byte[] request = "010C\r".getBytes();

                byte[] responseBuffer = new byte[1024];

                Connection.send(request);

                while(!Connection.hasData());

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Connection.readBuffer(outputStream);
                InputStream receiveStream = new ByteArrayInputStream(outputStream.toByteArray());

                receiveStream.read(responseBuffer);
                receiveStream.reset();
                receiveStream.close();

                String response = new String(responseBuffer, StandardCharsets.UTF_8);

                Log.d("Res", response);
            }
            catch (InterruptedException | IOException e) {
                Log.e("Err", "run: ", e);
            }
        }
    }
}
