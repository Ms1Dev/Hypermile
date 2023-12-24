package com.example.hypermile.obd;

import android.util.Log;

import com.example.hypermile.MainActivity;
import com.example.hypermile.bluetoothDevices.Connection;
import com.example.hypermile.data.DataManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class Obd {
    private final static int SEARCH_FOR_PROTOCOL_TIMEOUT = 20000;
    private final static int SEARCH_FOR_PROTOCOL_ATTEMPTS = 3;
    private final static int MINIMUM_RESPONSE_WAIT = 150;
    private final static int MAXIMUM_RESPONSE_WAIT = 500;
    private static boolean ready = false;

    private static final TreeMap<String,Pid> supportedPids = new TreeMap<>();

//    public static String getPids() {
//        StringBuilder sb = new StringBuilder();
//
//        for (byte pid : supportedPids) {
//            sb.append(String.format("%02X", pid)).append("\n");
//        }
//
//        return sb.toString();
//    }

    public static boolean initialise(Connection connection) {
        // elm327 documentation
        // https://www.elmelectronics.com/DSheets/ELM327DSH.pdf

        try {
            connection.sendCommand("ATD\r"); // set all defaults
            connection.sendCommand("ATZ\r"); // reset
            connection.sendCommand("ATE0\r"); // echo command off
            connection.sendCommand("ATL1\r"); // line feeds on
            connection.sendCommand("ATS1\r"); // spaces between bytes on
            connection.sendCommand("ATH1\r"); // headers on

            if (findProtocol(connection)) {
                Thread.sleep(500);
                getSupportedPids();
                ready = true;
            }

        } catch (InterruptedException | IOException e) {
            ready = false;
        }
        return ready;
    }

    public static Pid getPid(String pid) {
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
            if (obdFrame != null && obdFrame.isReponse()) return true;

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
                        Pid pid = new Pid(supportedPid);
                        supportedPids.put(pid.asString(), pid);
                    }
                }
            }
            Thread.sleep(200);
        }
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
        else {
            Log.d("TAG", "run: MISS");
        }

        return null;
    }


    public static boolean isReady() {
        return ready;
    }
}
