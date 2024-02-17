package com.example.hypermile.reports;

import com.example.hypermile.bluetooth.ConnectionEventListener;
import com.example.hypermile.bluetooth.ConnectionState;
import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataManager;
import com.example.hypermile.dataGathering.DataManagerReadyListener;

import java.util.Timer;
import java.util.TimerTask;

public class JourneyMonitor implements DataInputObserver<Double>, ConnectionEventListener, DataManagerReadyListener {
    private static final int STOPPED_ENGINE_ALLOWANCE_TIME = 10 * 1000;
    private long engineStoppedWhen;
    private DataManager dataManager;
    private Journey currentJourney;
    private Timer stopJourneyTimer;
    private boolean engineRunning = false;
    private boolean dataManagerIsReady = false;

    public JourneyMonitor(DataManager dataManager) {
        this.dataManager = dataManager;

//        // listen for changes in RPM to determine when the car has been started or stopped
//        dataManager.getEngineSpeed().addDataInputListener(this);
    }

    private void stopJourney() {
        if (currentJourney != null) {
            currentJourney.complete();
            currentJourney = null;
        }
    }

    private void startJourney() {
        if (currentJourney == null && dataManagerIsReady) {
            currentJourney = new Journey();
            currentJourney.addDataSource(dataManager.getEngineSpeed());
            currentJourney.addDataSource(dataManager.getSpeed());
            currentJourney.addDataSource(dataManager.getCalculatedMpg());
            currentJourney.addDataSource(dataManager.getCalculatedInclination());
            currentJourney.addDataSource(dataManager.getFuelRate());
            currentJourney.start(dataManager.getCurrentTimestamp());
            currentJourney.addLocationDataSource(dataManager.getCurrentLocation());
        }
    }

    @Override
    public void incomingData(Double data) {
        boolean engineRunningNow = data > 0.0;

        if (engineRunningNow) {
            startJourney();
            if (stopJourneyTimer != null) {
                stopJourneyTimer.cancel();
                stopJourneyTimer = null;
            }
        }
        else if (engineRunning && currentJourney != null) {
            engineStoppedWhen = System.currentTimeMillis();

            startTimerForJourneyEnd();
        }

        engineRunning = engineRunningNow;
    }

    private void startTimerForJourneyEnd() {
        if (stopJourneyTimer == null)  {
            stopJourneyTimer = new Timer();
            stopJourneyTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        stopJourney();
                    }
                },
                STOPPED_ENGINE_ALLOWANCE_TIME);
        }
    }

    @Override
    public void onStateChange(ConnectionState connectionState) {
        if (connectionState.equals(ConnectionState.DISCONNECTED)) {
            startTimerForJourneyEnd();
        }
        else if (connectionState.equals(ConnectionState.CONNECTED)) {
            startJourney();
        }
    }

    @Override
    public void dataManagerReady() {
        dataManagerIsReady = true;
        // listen for changes in RPM to determine when the car has been started or stopped
        dataManager.getEngineSpeed().addDataInputListener(this);
        startJourney();
    }
}
