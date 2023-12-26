package com.example.hypermile.obd;

import android.util.Log;

import java.util.Arrays;

public class ObdFrame {
    final static int ID_POS = 0;
    final static int PAYLOAD_SIZE_POS = 1;
    final static int RESPONSE_TYPE_POS = 2;
    final static int PID_POS = 3;
    final static int DATA_START_POS = 4;
    String[] frameData;
    int payloadSize = 0;
    boolean isResponse;
    byte pid;
    byte[] payload;


    public static ObdFrame createFrame(byte[] data) {
        String dataAsString = new String(data);
        Log.d("TAG", "createFrame: " + dataAsString);
        int idPos = dataAsString.indexOf("7E8");

        if (idPos == -1) return null;

        dataAsString = dataAsString.substring(idPos);
        String[] frameElements = dataAsString.split(" ");

        if (frameElements[0].equals("7E8")) {
            try {
                return new ObdFrame(frameElements);
            }
            catch (InvalidFrameException e) {
                Log.e("Err", "createFrame: Invalid frame", e);
                return null;
            }
        }
        return null;
    }

    public ObdFrame(String[] frameData) throws InvalidFrameException {
        this.frameData = frameData;

        try {
            payloadSize = (int) Integer.parseInt( frameData[PAYLOAD_SIZE_POS], 16);
            isResponse = frameData[RESPONSE_TYPE_POS].equals("41");
            pid = (byte) Integer.parseInt( frameData[PID_POS], 16 );
            payload = new byte[payloadSize - 2];
            for (int i = DATA_START_POS; i <= payloadSize + PAYLOAD_SIZE_POS; i++) {
                payload[i - DATA_START_POS] = (byte) Integer.parseInt(frameData[i], 16);
            }
        }
        catch (Exception e) {
            throw new InvalidFrameException(e.getMessage());
        }
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    public boolean isResponse() {
        return isResponse;
    }

    public byte getPid() {
        return pid;
    }

    public byte[] getPayload() {
        return payload;
    }
}
