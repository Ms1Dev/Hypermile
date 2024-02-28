package com.example.hypermile.bluetooth;

/**
 * When a bluetooth device is selected from the list this is a callback to the SelectBluetoothActivity
 */
public interface DeviceSelectedCallback {
    public void deviceSelected(DiscoveredDevice discoveredDevice);
}
