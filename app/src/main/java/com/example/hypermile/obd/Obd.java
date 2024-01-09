package com.example.hypermile.obd;

import android.util.Log;

import com.example.hypermile.bluetooth.Connection;
import com.example.hypermile.bluetooth.ConnectionEventListener;
import com.example.hypermile.bluetooth.ConnectionState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

public class Obd implements ConnectionEventListener {
    private final static int SEARCH_FOR_PROTOCOL_TIMEOUT = 20000;
    private final static int SEARCH_FOR_PROTOCOL_ATTEMPTS = 3;
    private final static int MINIMUM_RESPONSE_WAIT = 150;
    private final static int MAXIMUM_RESPONSE_WAIT = 500;
    private final static int MAX_ERRORS = 5;
    private int errors = 0;
    private boolean ready = false;
    private final TreeMap<String, Parameter> supportedPids = new TreeMap<>();
    final private ArrayList<ConnectionEventListener> connectionEventListeners = new ArrayList<>();
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private Connection connection;
    public Obd() {}

    public boolean initialise(Connection connection) {
        // elm327 documentation
        // https://www.elmelectronics.com/DSheets/ELM327DSH.pdf

        this.connection = connection;

        while(!connection.hasConnection());

        try {
            if (reset()) {
                Thread.sleep(MINIMUM_RESPONSE_WAIT);
                getSupportedPids();
                Thread.sleep(MINIMUM_RESPONSE_WAIT);
                connection.sendCommand("0902\r");
                Thread.sleep(MINIMUM_RESPONSE_WAIT);
                ready = true;
            }
        }
        catch (InterruptedException | IOException e) {
            ready = false;
            updateEventListeners(ConnectionState.ERROR);
        }
        updateEventListeners(ready? ConnectionState.CONNECTED : ConnectionState.ERROR);
        return ready;
    }

    private boolean reset() throws IOException, InterruptedException {
        updateEventListeners(ConnectionState.CONNECTING);
        connection.sendCommand("ATD\r"); // set all defaults
        connection.sendCommand("ATWS\r"); // reset
        connection.sendCommand("ATE0\r"); // echo command off
        connection.sendCommand("ATL1\r"); // line feeds on
        connection.sendCommand("ATS1\r"); // spaces between bytes on
        connection.sendCommand("ATH1\r"); // headers on
        return findProtocol();
    }

    public void addConnectionEventListener(ConnectionEventListener connectionEventListener) {
        connectionEventListeners.add(connectionEventListener);
    }

    public void removeConnectionEventListener(ConnectionEventListener connectionEventListener) {
        connectionEventListeners.remove(connectionEventListener);
    }

    private void updateEventListeners(ConnectionState connectionState) {
        this.connectionState = connectionState;
        for (ConnectionEventListener eventListener : connectionEventListeners) {
            eventListener.onStateChange(connectionState);
        }
    }

    public Parameter getPid(String pid) {
        return supportedPids.get(pid);
    }

    private boolean findProtocol() throws IOException, InterruptedException {

        int attempts = 0;

        do {
            long startMillis = System.currentTimeMillis();
            attempts++;
            connection.sendCommand("ATSP0\r"); // Auto detect protocol
            connection.sendCommand("0100\r"); // Request available PIDs (just to test for response)

            while (!connection.hasData()) {
                Thread.sleep(100);
                if(startMillis + SEARCH_FOR_PROTOCOL_TIMEOUT < System.currentTimeMillis()) {
                    break;
                }
            }
            ObdFrame obdFrame = connection.getLatestFrame();
            if (obdFrame != null) return true;

        } while (attempts < SEARCH_FOR_PROTOCOL_ATTEMPTS);
        return false;
    }

    private void getSupportedPids() throws InterruptedException {
        for (int i = 0x00; i <= 0xC8; i += 0x20) {
            String request = String.format("01%02x\r", i);

            byte[] response = requestData(request.getBytes());

            if (response == null) break;
            if (response.length != 4) break;

            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 8; k++) {
                    if ((response[j] & (1 << k)) > 0) {
                        byte supportedPid = (byte) (i + j * 8 + (8 - k));
                        Parameter parameter = new Parameter(supportedPid, this);
                        supportedPids.put(parameter.asString(), parameter);
                    }
                }
            }
            Thread.sleep(MINIMUM_RESPONSE_WAIT);
        }
    }

    public boolean supportsPid(String pid) {
        return supportedPids.containsKey(pid);
    }

    public String getVin() {
        byte[] vin = requestData("0902\r".getBytes());
        if (vin != null) {
            return new String(vin);
        }
        return null;
    }

    public byte[] requestObdData(byte[] requestCode) {
        if (!ready) return null;
        return requestData(requestCode);
    }

    private byte[] requestData(byte[] requestCode) {
        connection.send(requestCode);

        long currentMillis = System.currentTimeMillis();

        while(!connection.hasData() && currentMillis + MAXIMUM_RESPONSE_WAIT > System.currentTimeMillis());

        ObdFrame obdFrame = connection.getLatestFrame();

        while(currentMillis + MINIMUM_RESPONSE_WAIT > System.currentTimeMillis());

        if (obdFrame != null) {
            return obdFrame.getPayload();
        }
        return null;
    }

    public byte[] requestObdData(Parameter parameter) {
        if (!ready) return null;
        return requestData(parameter);
    }

    private byte[] requestData(Parameter parameter) {
        connection.send(parameter.getRequestCode());

        long currentMillis = System.currentTimeMillis();

        while(!connection.hasData() && currentMillis + MAXIMUM_RESPONSE_WAIT > System.currentTimeMillis());

        ObdFrame obdFrame = connection.getLatestFrame();

        while(currentMillis + MINIMUM_RESPONSE_WAIT > System.currentTimeMillis());

        if (obdFrame != null && obdFrame.getPid() == parameter.getId()) {
            return obdFrame.getPayload();
        }
        else {
            if (++errors > MAX_ERRORS) {
                ready = false;
                try {
                    ready = reset();
                }
                catch (InterruptedException | IOException e) {
                    ready = false;
                }
                updateEventListeners(ready? ConnectionState.CONNECTED : ConnectionState.ERROR);
                errors = 0;
            }
        }

        return null;
    }

    public boolean isReady() {
        return ready;
    }

    @Override
    public void onStateChange(ConnectionState connectionState) {
        if (connectionState != ConnectionState.CONNECTED) {
            ready = false;
            updateEventListeners(ConnectionState.DISCONNECTED);
        }
        else if (connectionState == ConnectionState.CONNECTED && !ready) {
            try {
                reset();
            } catch (IOException | InterruptedException e) {
                updateEventListeners(ConnectionState.ERROR);
            }
        }
    }
}
