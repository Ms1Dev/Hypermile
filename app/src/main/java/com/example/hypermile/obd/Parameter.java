package com.example.hypermile.obd;

public class Parameter {
    byte id;
    byte[] requestCode;
    Obd obd;

    Parameter(byte id, Obd obd) {
        this.id = id;
        this.obd = obd;
        requestCode = String.format("01%02X\r", id).getBytes();
    }

    public String asString() {
        return String.format("%02X", id);
    }

    public byte[] getRequestCode() {
        return requestCode;
    }

    public byte[] getData() {
        if (obd.isReady()) {
            return obd.requestObdData(this);
        }
        return null;
    }

    public byte getId() {
        return id;
    }
}
