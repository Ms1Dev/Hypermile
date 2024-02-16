package com.example.hypermile.bluetooth;

/**
 * Interface for classes that need to be notified on connection state changes.
 */
public interface ConnectionEventListener {
    public void onStateChange(ConnectionState connectionState);
}
