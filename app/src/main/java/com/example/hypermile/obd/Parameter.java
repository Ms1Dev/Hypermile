package com.example.hypermile.obd;

/**
 * Represents a vehicle parameter which is usually sensor data
 */
public class Parameter {
    byte id;
    byte[] requestCode;
    Obd obd;

    Parameter(byte id, Obd obd) {
        this.id = id;
        this.obd = obd;
        // Request for sensor data is 01 followed by the PID
        // e.g. a request code of 0110 will request data from parameter 0x10
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
