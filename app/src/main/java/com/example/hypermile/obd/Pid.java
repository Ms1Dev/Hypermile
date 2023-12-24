package com.example.hypermile.obd;

public class Pid {
    byte id;
    byte[] requestCode;

    Pid(byte id) {
        this.id = id;
        requestCode = String.format("01%02X\r", id).getBytes();
    }

    public String asString() {
        return String.format("%02X", id);
    }

    public byte[] getRequestCode() {
        return requestCode;
    }

    public byte[] getData() {
        if (Obd.isReady()) {
            return Obd.requestObdData(requestCode);
        }
        return null;
    }
}
