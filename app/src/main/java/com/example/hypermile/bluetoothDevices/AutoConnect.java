package com.example.hypermile.bluetoothDevices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class AutoConnect {
    private static final int CONNECTION_ATTEMPTS = 5;
    private static final int MONITOR_FREQUENCY = 5000;
    private static final String PREFERENCE_FILENAME = "Hypermile_preferences";
    private static final String PREFERENCE_DEVICE_MAC = "ConnectedDeviceMAC";
    private int connectAttempts = 0;
    BluetoothDevice bluetoothDevice;
    Context context;

    public AutoConnect(Context context) {
        this.context = context;

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(PREFERENCE_DEVICE_MAC)) {
            BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

            try {
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(sharedPreferences.getString(PREFERENCE_DEVICE_MAC, null));
            }
            catch (IllegalArgumentException e) {
                Log.e("Err", "AutoConnect: Stored MAC address invalid", e);
            }
        }

        monitorConnection();
    }

    private void monitorConnection() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection connection = Connection.getInstance();
                while(true) {
                    try {
                        Thread.sleep(MONITOR_FREQUENCY);
                        ConnectionState connectionState = connection.getConnectionState();
                        if (connectionState == ConnectionState.DISCONNECTED && connectAttempts < CONNECTION_ATTEMPTS &&  bluetoothDevice != null)
                        {
                            connectAttempts++;
                            connection.createConnection(bluetoothDevice);
                        }
                        else if (connectionState == ConnectionState.CONNECTED) {
                            connectAttempts = 0;
                        }
                    } catch (InterruptedException e) {
                        //TODO:
                    }
                }
            }
        }).start();
    }

}
