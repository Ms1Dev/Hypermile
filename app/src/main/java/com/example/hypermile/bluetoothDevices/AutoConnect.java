package com.example.hypermile.bluetoothDevices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class AutoConnect {
    private static final int MONITOR_FREQUENCY = 5000;
    private static final String PREFERENCE_FILENAME = "Hypermile_preferences";
    private static final String PREFERENCE_DEVICE_MAC = "ConnectedDeviceMAC";
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
                while(true) {
                    try {
                        Thread.sleep(MONITOR_FREQUENCY);
                        Connection connection = Connection.getInstance();
                        if (connection.getConnectionState() == ConnectionState.DISCONNECTED && bluetoothDevice != null) {
                            connection.createConnection(bluetoothDevice);
                        }
                    } catch (InterruptedException e) {
                        //TODO:
                    }
                }
            }
        }).start();
    }

}
