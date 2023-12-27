package com.example.hypermile.obd;

import android.util.Log;

import com.example.hypermile.bluetooth.Connection;

import java.io.IOException;
import java.util.TreeMap;

public class Obd {
    private final static int SEARCH_FOR_PROTOCOL_TIMEOUT = 20000;
    private final static int SEARCH_FOR_PROTOCOL_ATTEMPTS = 3;
    private final static int MINIMUM_RESPONSE_WAIT = 150;
    private final static int MAXIMUM_RESPONSE_WAIT = 500;
    private final static int MAX_ERRORS = 5;
    private static int errors = 0;
    private static boolean ready = false;
    private static final TreeMap<String, Parameter> supportedPids = new TreeMap<>();

    public static boolean initialise(Connection connection) {
        // elm327 documentation
        // https://www.elmelectronics.com/DSheets/ELM327DSH.pdf
        
        try {
            if (reset(connection)) {
                Thread.sleep(200);
                getSupportedPids();
                Thread.sleep(200);
                connection.sendCommand("0902\r");
                Thread.sleep(200);
                ready = true;
            }
        }
        catch (InterruptedException | IOException e) {
            ready = false;
        }
        return ready;
    }

    private static boolean reset(Connection connection) throws IOException, InterruptedException {
        connection.sendCommand("ATD\r"); // set all defaults
        connection.sendCommand("ATWS\r"); // reset
        connection.sendCommand("ATE0\r"); // echo command off
        connection.sendCommand("ATL1\r"); // line feeds on
        connection.sendCommand("ATS1\r"); // spaces between bytes on
        connection.sendCommand("ATH1\r"); // headers on
        return findProtocol(connection);
    }

    public static Parameter getPid(String pid) {
        return supportedPids.get(pid);
    }

    private static boolean findProtocol(Connection connection) throws IOException, InterruptedException {

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

    private static void getSupportedPids() throws InterruptedException {
        for (int i = 0x00; i <= 0xC8; i += 0x20) {
            String request = String.format("01%02x\r", i);

            byte[] response = requestObdData(request.getBytes());

            if (response == null) break;
            if (response.length != 4) break;

            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 8; k++) {
                    if ((response[j] & (1 << k)) > 0) {
                        byte supportedPid = (byte) (i + j * 8 + (8 - k));
                        Parameter parameter = new Parameter(supportedPid);
                        supportedPids.put(parameter.asString(), parameter);

                        Log.d("TAG", "Supported Pid: " + parameter.asString());

                    }
                }
            }
            Thread.sleep(200);
        }
    }

    public static boolean supportsPid(String pid) {
        return supportedPids.containsKey(pid);
    }

    public static String getVin() {
        byte[] vin = requestObdData("0902\r".getBytes());
        return new String(vin);
    }

    public static byte[] requestObdData(byte[] requestCode) {
        Connection connection = Connection.getInstance();

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

    public static byte[] requestObdData(Parameter parameter) {
        Connection connection = Connection.getInstance();

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
                    ready = reset(connection);
                }
                catch (InterruptedException | IOException e) {
                    ready = false;
                }
                errors = 0;
            }
        }

        return null;
    }

    public static boolean isReady() {
        return ready;
    }
}
