package com.example.hypermile.obd;

import com.example.hypermile.bluetooth.Connection;
import com.example.hypermile.bluetooth.ConnectionEventListener;
import com.example.hypermile.bluetooth.ConnectionState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Manages communication with the OBD scanner once a bluetooth connection is available.
 */
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
    private Connection connection;
    public Obd() {}

    /**
     * Initialise the communication with the vehicle
     * See elm327 documentation for more information on commands: https://www.elmelectronics.com/DSheets/ELM327DSH.pdf
     * @param connection
     * @return boolean
     */
    public boolean initialise(Connection connection) {
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

    /**
     * Resets the connection and configures scanner
     * @return boolean
     * @throws IOException
     * @throws InterruptedException
     */
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
        for (ConnectionEventListener eventListener : connectionEventListeners) {
            eventListener.onStateChange(connectionState);
        }
    }

    public Parameter getPid(String pid) {
        return supportedPids.get(pid);
    }

    /**
     * Uses the elm327 built in auto scan function to detect the vehicles CANBUS protocol
     * @return boolean
     * @throws IOException
     * @throws InterruptedException
     */
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

    /**
     * Get the IDs of supported parameters from the vehicle
     * @throws InterruptedException
     */
    private void getSupportedPids() throws InterruptedException {
        /*
         * This nested for loop is quite ugly so I'll explain...
         * To know which sensors or parameter IDs (PIDs) a vehicle supports it will respond with binary data representing those PIDs
         * The number of bits from the MSB is the PID that is supported
         * For example, from the following byte 00001001 the 5th and 8th bits are set
         * This would mean PID 0x05 and 0x08 are supported.
         *
         * Each response covers 4 bytes containing 32 PIDs out of 200 PIDs in total
         *
         * i: each 4 byte response
         * j: each byte of that response
         * k: each bit of that byte
         *
         * Adding up all the offsets to get the value of the PID: i + j * 8 + (8 - k)
         *
         * For more info visit: https://en.wikipedia.org/wiki/OBD-II_PIDs#Service_01_PID_00_-_Show_PIDs_supported
         */
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
            String vinStr = new String(vin);
            vinStr = vinStr.replaceAll("[^a-zA-Z\\d]", "");
            return vinStr;
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
