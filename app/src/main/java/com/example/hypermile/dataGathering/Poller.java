package com.example.hypermile.dataGathering;

import android.util.Log;

import java.util.ArrayList;

/**
 * Data poller
 * Calls the "sampleData" method on pollingElement classes at a regular interval.
 * Also notifies pollCompleteListeners after each round of polling.
 */
public class Poller extends Thread {
    private int sleepDuration = 500;
    ArrayList<PollingElement> pollingElements = new ArrayList<>();
    ArrayList<PollCompleteListener> pollCompleteListeners = new ArrayList<>();


    public Poller(int sampleRateHz) {
        sleepDuration = 1000 / sampleRateHz;
    }

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

    public void run() {
        while(true) {
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
