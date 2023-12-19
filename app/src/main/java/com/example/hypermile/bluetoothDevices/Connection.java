package com.example.hypermile.bluetoothDevices;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.UUID;


// Singleton class since we only ever have one bluetooth device connected
public class Connection {
    private static Connection instance;
    private static ConnectionThread connectionThread;
    private static BluetoothDevice bluetoothDevice;
    private static byte[] inputBuffer = new byte[1024];
    private static int newDataLen = 0;
    private static ConnectionState connectionState = ConnectionState.DISCONNECTED;
    final static private ArrayList<ConnectionEventListener> connectionEventListeners = new ArrayList<>();

    private Connection(){}

    public void addConnectionEventListener(ConnectionEventListener connectionEventListener) {
        connectionEventListeners.add(connectionEventListener);
    }

    public void removeConnectionEventListener(ConnectionEventListener connectionEventListener) {
        connectionEventListeners.remove(connectionEventListener);
    }

    private void updateEventListeners(ConnectionState connectionState) {
        this.connectionState = connectionState;
        for (ConnectionEventListener eventListener : connectionEventListeners) {
            eventListener.onStateChange(connectionState);
        }
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    public static Connection getInstance() {
        if (instance == null) {
            instance = new Connection();
        }
        return instance;
    }
    public boolean hasConnection() {
        return connectionThread != null;
    }
    public boolean hasData() {
        return newDataLen > 0;
    }

    /**
     * Reads the input buffer into a bytestream
     * @param out
     * @throws IOException
     */
    public void readBuffer(ByteArrayOutputStream out) throws IOException {
        out.write(inputBuffer);
        out.write('\0');
        out.flush();
        out.close();
        newDataLen = 0;
        for (int i = 0; i < 1024; i++) {
            inputBuffer[i] = 0;
        }
    }

    /**
     * Sends the byte array to the bluetooth device
     * @param command
     */
    public void send(byte[] command) {
        if (hasConnection()) {
            connectionThread.write(command);
        }
    }

    /**
     * Attempts to create connection with a bluetooth device
     * @param bluetoothDevice
     */
    public void createConnection(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
        if (connectionThread != null) {
            connectionThread.cancel();
        }
        InitConnThread initConnThread = new InitConnThread(bluetoothDevice);
        initConnThread.start();
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    /**
     * Spawns a ConnectionThread which manages communication with the device
     * @param bluetoothSocket
     */
    private void manageConnection(BluetoothSocket bluetoothSocket) {
        connectionThread = new ConnectionThread(bluetoothSocket);
        connectionThread.start();
    }


    /**
     * Thread for attempting to create a connection with a bluetooth device.
     * Will call manageConnection() if successful.
     */
    private class InitConnThread extends Thread {
        private final BluetoothSocket bluetoothSocket;

        public InitConnThread(BluetoothDevice bluetoothDevice) {
            BluetoothSocket _socket = null;
            try {
                //TODO: figure out why this UUID is used
                _socket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            }
            catch (IOException | SecurityException e) {
                Log.d("Err", "ConnectThread: Failed to create socket");
            }
            bluetoothSocket = _socket;
        }


        public void run() {
            updateEventListeners(ConnectionState.CONNECTING);

            try {
                bluetoothSocket.connect();
            } catch (IOException | SecurityException connectException) {
                updateEventListeners(ConnectionState.DISCONNECTED);
                try {
                    bluetoothSocket.close();
                } catch (IOException closeException) {
                    Log.e("Err", "Could not close the client socket", closeException);
                }
                return;
            }
            manageConnection(bluetoothSocket);
        }
    }

    /**
     * Manages the connection and communication with the device.
     */
    private class ConnectionThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectionThread(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e("Err", "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("Err", "Error occurred when creating output stream", e);
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            updateEventListeners(ConnectionState.CONNECTED);
            while (true) {
                try {
                    newDataLen = inputStream.read(inputBuffer);

//                    String response = new String(inputBuffer);
//
//                    Log.d("Res", response);
                } catch (IOException e) {
                    Log.d("Err", "Input stream was disconnected", e);
                    cancel();
                    break;
                }
            }
        }


        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.e("Err", "Error occurred when sending data", e);
            }
        }


        public void cancel() {
            try {
                bluetoothSocket.close();
                connectionThread = null;
                updateEventListeners(ConnectionState.DISCONNECTED);
            } catch (IOException e) {
                Log.e("Err", "Could not close the connect socket", e);
            }
        }
    }

}
