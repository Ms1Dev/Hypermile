package com.example.hypermile.bluetoothDevices;


public class DiscoveredDevice {
    private String name;
    private String macAddress;

    public DiscoveredDevice(String name, String macAddress) {
        this.name = name;
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }
}