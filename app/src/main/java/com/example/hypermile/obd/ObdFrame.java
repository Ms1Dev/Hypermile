package com.example.hypermile.obd;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class ObdFrame {
    final static String CAN_ID = "7E8";
    final static int PAYLOAD_SIZE_POS = 1;
    final static int PID_POS = 3;
    final static int DATA_START_POS = 4;
    String[] frameData;
    int bytesToRead = 0;
    boolean expectingMoreLines = false;
    byte pid;
    ArrayList<Byte> dataIn = new ArrayList<>();


    public static ObdFrame createFrame(String data) {
        int idPos = data.indexOf(CAN_ID);

        if (idPos == -1) return null;

        data = data.substring(idPos);
        String[] frameElements = data.split("\r");

        try {
            return new ObdFrame(frameElements);
        }
        catch (InvalidFrameException e) {
            Log.e("Err", "createFrame: Invalid frame", e);
            return null;
        }
    }

    public ObdFrame(String[] frames) throws InvalidFrameException {
        this.frameData = frames;

        for (String frame : frames) {
            try {
                String[] frameData = frame.split(" ");

                int firstByte = Integer.parseInt(frameData[1], 16);
                int dataStartPos = DATA_START_POS;

                // Normal response
                if (firstByte < 0x10) {
                    bytesToRead = Integer.parseInt( frameData[PAYLOAD_SIZE_POS], 16) - 2;
                    pid = (byte) Integer.parseInt( frameData[PID_POS], 16 );
                }
                // multi line response
                else if (firstByte == 0x10) {
                    expectingMoreLines = true;
                    bytesToRead = Integer.parseInt( frameData[PAYLOAD_SIZE_POS + 1], 16) - 2;
                    pid = (byte) Integer.parseInt( frameData[PID_POS + 1], 16 );
                    dataStartPos += 1;
                }
                // extra line for multi line response
                else {
                    expectingMoreLines = true;
                    dataStartPos -= 2;
                }

                int index = dataStartPos;
                while(bytesToRead > 0 && index <= 8) {
                    dataIn.add((byte) Integer.parseInt(frameData[index], 16));
                    index++;
                    if (--bytesToRead == 0) {
                        expectingMoreLines = false;
                    }
                }

                if (!expectingMoreLines) break;
            }
            catch (Exception e) {
                Log.d("TAG", "Error frame: " + Arrays.toString(frameData));
                throw new InvalidFrameException(e.getMessage());
            }
        }
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
