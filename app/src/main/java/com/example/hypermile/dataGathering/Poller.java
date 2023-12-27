package com.example.hypermile.dataGathering;

import android.util.Log;

import com.example.hypermile.bluetooth.Connection;

import java.util.ArrayList;

public class Poller extends Thread {
    private final static int RESPONSE_DELAY = 100;
    private int sleepDuration = 500;
    ArrayList<PollingElement> pollingElements = new ArrayList<>();
    ArrayList<PollCompleteListener> pollCompleteListeners = new ArrayList<>();

    public void addPollingElement(PollingElement pollingElement) {
        if (pollingElement != null) {
            pollingElements.add(pollingElement);
        }
    }

    public void addPollCompleteListener(PollCompleteListener pollCompleteListener) {
        if (pollCompleteListener != null) {
            this.pollCompleteListeners.add(pollCompleteListener);
        }
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