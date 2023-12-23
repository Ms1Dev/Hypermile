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
    ArrayList<PollingElement> pollingElements = new ArrayList<>();
    ArrayList<PollCompleteListener> pollCompleteListeners = new ArrayList<>();

    public void addPollingElement(PollingElement pollingElement) {
        pollingElements.add(pollingElement);
    }

    public void addPollCompleteListener(PollCompleteListener pollCompleteListener) {
        this.pollCompleteListeners.add(pollCompleteListener);
    }

    private void pollingComplete() {
        for (PollCompleteListener pollCompleteListener : pollCompleteListeners) {
            pollCompleteListener.pollingComplete();
        }
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

                    pollingComplete();

                } catch (InterruptedException e) {
                    Log.e("Err", "run: ", e);
                }
            }
        }
    }
}
