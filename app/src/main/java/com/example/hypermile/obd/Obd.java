package com.example.hypermile.obd;

import android.util.Log;

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
    private final static int SEARCH_FOR_PROTOCOL_TIMEOUT = 10000;
    private final static int SEARCH_FOR_PROTOCOL_ATTEMPTS = 5;
    private final static int MINIMUM_RESPONSE_WAIT = 150;
    private final static int MAXIMUM_RESPONSE_WAIT = 500;
    private final static int MAX_ERRORS = 5;
    private int errors = 0;
    private boolean ready = false;
    private boolean initialised = false;
    private final TreeMap<String, Parameter> supportedPids = new TreeMap<>();
    final private ArrayList<ConnectionEventListener> connectionEventListeners = new ArrayList<>();
    private Connection connection;
    public Obd() {}

    /**
     * Initialise the communication with the vehicle
     * @param connection
     * @return boolean
     */
    public void initialise(Connection connection) {
        this.connection = connection;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!connection.hasConnection());
                try {
                    if (reset()) {
                        Thread.sleep(MINIMUM_RESPONSE_WAIT);
                        getSupportedPids();
                        Thread.sleep(MINIMUM_RESPONSE_WAIT);
                        connection.sendCommand("0902\r");
                        Thread.sleep(MINIMUM_RESPONSE_WAIT);
                        ready = true;
                        initialised = true;
                    }
                }
                catch (InterruptedException | IOException e) {
                    ready = false;
                    updateEventListeners(ConnectionState.ERROR);
                }
                updateEventListeners(ready? ConnectionState.CONNECTED : ConnectionState.ERROR);
            }
        }).start();
    }

    /**
     * Resets the connection and configures scanner
     * Various AT commands are used to configure the scanner
     * See elm327 documentation page 9 onwards for more information on commands: https://www.elmelectronics.com/DSheets/ELM327DSH.pdf
     * @return boolean
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean reset() throws IOException, InterruptedException {
        updateEventListeners(ConnectionState.CONNECTING);
        connection.sendCommand("ATZ\r"); // reset
        connection.sendCommand("ATD\r"); // set all defaults
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
            connection.sendCommand("ATSP0\r"); // Auto detect protocol command
            connection.sendCommand("0100\r"); // Request available PIDs 0x00 - 0x20 (just to check for response)

            while (!connection.hasData()) {
                Thread.sleep(100);
                if(startMillis + SEARCH_FOR_PROTOCOL_TIMEOUT < System.currentTimeMillis()) {
                    break;
                }
            }
            ObdFrame obdFrame = connection.getLatestFrame();

            if (obdFrame != null) {
                return true;
            }

        } while (attempts < SEARCH_FOR_PROTOCOL_ATTEMPTS);
        return false;
    }

    /**
     * Get the IDs of supported parameters from the vehicle
     * @throws InterruptedException
     */
    private void getSupportedPids() throws InterruptedException {
        /*
         * This nested FOR loop is quite ugly so I'll explain...
         * To know which sensors AKA parameter IDs (PIDs) a vehicle supports it will respond with binary data representing those PIDs
         * The number of bits from the left (MSB) is the number of the PID that is supported
         * For example, from the following byte 00001001 the 5th and 8th bits are set
         * This would mean PID 0x05 and 0x08 are supported.
         *
         * Each response covers 4 bytes containing 32 PIDs
         *
         * i: the start offset of each 4 byte response
         * j: each byte of that response
         * k: each bit of that byte
         *
         * Adding up all the offsets to get the value of the PID: i + j * 8 + (8 - k)
         * then AND it with the response to check whether it is available
         *
         * For more info visit: https://en.wikipedia.org/wiki/OBD-II_PIDs#Service_01_PID_00_-_Show_PIDs_supported
         */
        for (int i = 0x00; i <= 0xC8; i += 0x20) { // increments of 0x20 or 32 decimal
            String request = String.format("01%02x\r", i);

            byte[] response = requestData(request.getBytes());

            if (response == null) break;
            if (response.length != 4) break;

            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 8; k++) {
                    if ((response[j] & (1 << k)) > 0) { // if the value of this bit is greater than 0 this pid is available
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

    /**
     * Request the VIN of the vehicle
     * @return
     */
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

    /**
     * Request data using a request code instead of a Parameter object
     */
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

    /**
     * Requests OBD data but with additional check to make sure OBD is ready
     */
    public byte[] requestObdData(Parameter parameter) {
        if (!ready) return null;
        return requestData(parameter);
    }

    /**
     * Request data from the OBD device for the given parameter
     */
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

    /**
     * Listen to changes of the bluetooth connection. OBD cannot communicate without bluetooth
     */
    @Override
    public void onStateChange(ConnectionState connectionState) {
        if (connectionState != ConnectionState.CONNECTED) {
            ready = false;
            updateEventListeners(ConnectionState.DISCONNECTED);
        }
        else if (!ready && initialised) {
            try {
                ready = reset();
                updateEventListeners(ready? ConnectionState.CONNECTED : ConnectionState.ERROR);
                errors = 0;
            } catch (IOException | InterruptedException e) {
                updateEventListeners(ConnectionState.ERROR);
            }
        }
    }
}
