package com.example.hypermile.obd;

import android.util.Log;

import java.util.ArrayList;
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
    int bytesToRead = 0;
    boolean expectingMoreLines = false;
    boolean isMultiline = false;
    boolean isExtraLine = false;
    byte pid;
//    byte[] payload;
    ArrayList<Byte> dataIn = new ArrayList<>();


    public static ObdFrame createFrame(String data) {
        int idPos = data.indexOf("7E8");

        if (idPos == -1) return null;

        data = data.substring(idPos);
        String[] frameElements = data.split(" ");

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
            int firstByte = Integer.parseInt(frameData[1], 16);

            Log.d("TAG", "firstByte: " + firstByte);

            int dataStartPos = DATA_START_POS;

            if (firstByte < 0x10) {
                payloadSize = Integer.parseInt( frameData[PAYLOAD_SIZE_POS], 16);
                bytesToRead = payloadSize -2;
                pid = (byte) Integer.parseInt( frameData[PID_POS], 16 );

            } else if (firstByte == 0x10) {
                expectingMoreLines = true;
                payloadSize = Integer.parseInt( frameData[PAYLOAD_SIZE_POS + 1], 16);
                bytesToRead = payloadSize -2;
                dataStartPos++;
                pid = (byte) Integer.parseInt( frameData[PID_POS + 1], 16 );
            }

//            isResponse = frameData[RESPONSE_TYPE_POS].equals("41");
//            payload = new byte[payloadSize - 2];

            int index = dataStartPos;
            while(bytesToRead-- > 0 && index <= 8) {
                dataIn.add((byte) Integer.parseInt(frameData[index], 16));
                index++;
            }

            Log.d("TAG", " frame: " + Arrays.toString(dataIn.toArray()));

//            for (int i = DATA_START_POS; i <= payloadSize + PAYLOAD_SIZE_POS; i++) {
//                payload[i - DATA_START_POS] = (byte) Integer.parseInt(frameData[i], 16);
//            }
        }
        catch (Exception e) {
            Log.d("TAG", "Error frame: " + Arrays.toString(frameData));
            throw new InvalidFrameException(e.getMessage());
        }
    }

    public void append(String data) {
        String[] frame = data.split(" ");

        int index = 2;
        while(bytesToRead-- > 0 && index <= 8) {
            dataIn.add((byte) Integer.parseInt(frame[index], 16));
            index++;
            if (bytesToRead == 0) {
                expectingMoreLines = false;
            }
        }

        Log.d("TAG", " multiline: " + Arrays.toString(dataIn.toArray()));
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    public boolean isResponse() {
        return isResponse;
    }

    public boolean isExpectingMoreLines() {
        return expectingMoreLines;
    }

    public byte getPid() {
        return pid;
    }

    public byte[] getPayload() {
        byte[] payload = new byte[dataIn.size()];
        for(int i = 0; i < dataIn.size(); i++)  {
            payload[i] = dataIn.get(i);
        }
        return payload;
    }
}
