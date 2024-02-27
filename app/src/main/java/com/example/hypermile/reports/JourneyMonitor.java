package com.example.hypermile.reports;

import com.example.hypermile.bluetooth.ConnectionEventListener;
import com.example.hypermile.bluetooth.ConnectionState;
import com.example.hypermile.dataGathering.DataInputObserver;
import com.example.hypermile.dataGathering.DataManager;
import com.example.hypermile.dataGathering.DataManagerReadyListener;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Listens out for events that indicate the start and end of a journey, i.e. engine start/stop, connection lost
 * Creates new journey every time a journey start condition is detected
 * On journey end it calls the Journey.complete() method which in turn writes the journey to Firestore
 */
public class JourneyMonitor implements DataInputObserver<Double>, ConnectionEventListener, DataManagerReadyListener {
    private static final int STOPPED_ENGINE_ALLOWANCE_TIME = 10 * 1000;
    private final DataManager dataManager;
    private Journey currentJourney;
    private Timer stopJourneyTimer;
    private boolean engineRunning = false;
    private boolean dataManagerIsReady = false;

    public JourneyMonitor(DataManager dataManager) {
        this.dataManager = dataManager;
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

    /**
     * Data listener method
     * Listening for changes in RPM to detect engine stop/start
     */
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
            startTimerForJourneyEnd();
        }

        engineRunning = engineRunningNow;
    }

    /**
     * When the end of a journey is detected then wait before completing the journey
     * This is to allow for the car stalling or temporary loss of connection
     */
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

    /**
     * Listen for connection state changes to detect connection ending
     */
    @Override
    public void onStateChange(ConnectionState connectionState) {
        if (connectionState.equals(ConnectionState.DISCONNECTED)) {
            startTimerForJourneyEnd();
        }
        else if (connectionState.equals(ConnectionState.CONNECTED) && engineRunning) {
            startJourney();
        }
    }

    /**
     * The DataManager must be ready before starting a journey and to allow listening to RPM
     */
    @Override
    public void dataManagerReady() {
        dataManagerIsReady = true;
        // listen for changes in RPM to determine when the car has been started or stopped
        dataManager.getEngineSpeed().addDataInputListener(this);
        startJourney();
    }
}
