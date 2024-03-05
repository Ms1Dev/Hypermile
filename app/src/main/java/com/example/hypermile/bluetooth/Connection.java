package com.example.hypermile.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import com.example.hypermile.obd.Obd;
import com.example.hypermile.obd.ObdFrame;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Class for creating and maintaining a Bluetooth connection.
 * Spawns an InitConnThread object for initialising the connection and getting the Bluetooth socket.
 * When a bluetooth socket is created a ConnectionThread object is created.
 * The ConnectionThread creates input and output streams connected to the bluetooth socket to allow communication with the device.
 * A timer object also monitors the connection and attempts to reconnect if it becomes disconnected.
 */
public class Connection {
    private static final int CONNECTION_ATTEMPTS = 3;
    private static final int MONITOR_FREQUENCY = 5000;
    private static final String PREFERENCE_FILENAME = "Hypermile_preferences";
    private static final String PREFERENCE_DEVICE_MAC = "ConnectedDeviceMAC";
    private ConnectionThread connectionThread;
    private InitConnThread initConnThread;
    private BluetoothDevice bluetoothDevice;
    private ObdFrame latestFrame;
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private boolean isConnected = false;
    private int autoConnectAttempts = 0;
    private Timer autoConnectTimer;
    final private ArrayList<ConnectionEventListener> connectionEventListeners = new ArrayList<>();

    public Connection(){}

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

    public void disconnect() {
        if (initConnThread != null) {
            initConnThread.cancel();
        }
        if (connectionThread != null) {
            connectionThread.cancel();
        }
        if (autoConnectTimer != null) {
            autoConnectTimer.cancel();
        }
    }

    public void autoConnectFailed() {
        autoConnectTimer.cancel();
        updateEventListeners(ConnectionState.ERROR);
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public boolean hasConnection() {
        return isConnected && connectionThread != null;
    }

    public boolean hasData() {
        return latestFrame != null;
    }

    /**
     * Tries to connect to an existing MAC address if it is stored in shared preferences.
     */
    public void connectToExisting(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(PREFERENCE_DEVICE_MAC)) {
            BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            try {
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(sharedPreferences.getString(PREFERENCE_DEVICE_MAC, null));
                createConnection(bluetoothDevice);
            }
            catch (IllegalArgumentException e) {
                Log.e("Err", "AutoConnect: Stored MAC address invalid", e);
            }
        }
    }

    /**
     * Returns the latest frame of data received by the OBD device.
     * Clears the frame so it can only be retrieved once.
     */
    public ObdFrame getLatestFrame() {
        ObdFrame obdFrame = latestFrame;
        latestFrame = null;
        return obdFrame;
    }


    /**
     * Sends the byte array to the bluetooth device
     */
    public void send(byte[] command) {
        if (hasConnection()) {
            connectionThread.write(command);
        }
    }

    /**
     * This is called when a device is selected from the bluetooth device list.
     */
    public void manuallySelectedConnection(BluetoothDevice bluetoothDevice) {
        autoConnectAttempts = 0;
        createConnection(bluetoothDevice);
    }

    /**
     * Attempts to create connection with a bluetooth device
     */
    private void createConnection(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice == null) return;
        this.bluetoothDevice = bluetoothDevice;

        // cancel any existing threads to prevent conflicts
        if (initConnThread != null) {
            initConnThread.cancel();
        }
        if (connectionThread != null) {
            connectionThread.interrupt();
        }

        updateEventListeners(ConnectionState.CONNECTING);

        initConnThread = new InitConnThread(bluetoothDevice);
        initConnThread.start();

        autoConnect();
    }


    /**
     * Spawns a ConnectionThread which manages communication with the device
     * Updates connection event listeners to CONNECTED
     */
    private void manageConnection(BluetoothSocket bluetoothSocket) {
        connectionThread = new ConnectionThread(bluetoothSocket);
        connectionThread.start();
        isConnected = true;

        updateEventListeners(ConnectionState.CONNECTED);
    }

    /**
     * Will monitor the connection and attempt to reconnect if it is disconnected.
     */
    private void autoConnect() {
        autoConnectTimer = new Timer();
        autoConnectTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ConnectionState connectionState = getConnectionState();
                if (connectionState != ConnectionState.CONNECTED && connectionState != ConnectionState.CONNECTING  && autoConnectAttempts < CONNECTION_ATTEMPTS &&  bluetoothDevice != null){
                    autoConnectAttempts++;
                    createConnection(bluetoothDevice);
                }
                else if (connectionState == ConnectionState.CONNECTED) {
                    autoConnectAttempts = 0;
                }
                else if (autoConnectAttempts >= CONNECTION_ATTEMPTS) {
                    autoConnectFailed();
                }
            }
        }, 0, 5000);
    }

    /**
     * Send a command using the connection thread.
     */
    public boolean sendCommand(String command) throws IOException, InterruptedException {
        byte[] cmdAsBytes = command.getBytes();
        if (connectionThread != null) {
            connectionThread.write(cmdAsBytes);
            Thread.sleep(200);
            return true;
        }
        return false;
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
                // UUID for bluetooth from this source: https://developer.android.com/reference/android/bluetooth/BluetoothDevice#createInsecureRfcommSocketToServiceRecord(java.util.UUID)
                _socket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            }
            catch (IOException | SecurityException e) {
                Log.e("Err", "Connection error", e);
            }
            bluetoothSocket = _socket;
        }


        public void run() {
            try {
                bluetoothSocket.connect();
            }
            catch (IOException | SecurityException connectException) {
                updateEventListeners(ConnectionState.ERROR);
                try {
                    bluetoothSocket.close();
                } catch (IOException closeException) {
                    Log.e("Err", "Could not close bluetooth socket", closeException);
                }
                return;
            }
            catch (NullPointerException e) {
                Log.e("Err", "Bluetooth socket null: ", e);
                updateEventListeners(ConnectionState.ERROR);
                return;
            }
            manageConnection(bluetoothSocket);
        }

        /**
         * In case the connection needs to be cancelled before a connection has been established
         */
        public void cancel() {
            try {
                bluetoothSocket.close();
                isConnected = false;
            } catch (IOException closeException) {
                Log.e("Err", "Could not close the client socket", closeException);
            }
            this.interrupt();
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
            // because the streams are final temporary ones are needed
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

        /**
         * This run method is monitoring the socket for incoming messages and will continue to run while there is a connection.
         */
        public void run() {
            while (!isInterrupted()) {
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    int readValue;
                    while ((readValue = inputStream.read()) != -1) {
                        char charValue = (char) readValue;
                        if (charValue == '>') break; // this character is the end of a message from OBD
                        stringBuilder.append(charValue);
                    }

                    // if message was received try to make an OBD frame from it
                    if (stringBuilder.length() > 0) {
                        ObdFrame obdFrame = ObdFrame.createFrame(stringBuilder.toString());

                        // if its a valid frame then store it as the latest frame
                        if (obdFrame != null) latestFrame = obdFrame;
                    }

                } catch (IOException e) {
                    Log.d("Err", "Input stream was disconnected", e);
                    cancel();
                    break;
                }
            }
        }

        /**
         * Write data out over bluetooth.
         * This clears the latest received frame because the outbound message always expects a response
         * so anything that's left in there from before is irrelevant and may confuse things
         */
        public void write(byte[] bytes) {
            try {
                latestFrame = null;
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.e("Err", "Error occurred when sending data", e);
            }
        }

        /**
         * If the bluetooth connection needs to be cancelled then close the socket and set the appropriate statuses
         */
        public void cancel() {
            try {
                bluetoothSocket.close();
                interrupt();
                connectionThread = null;
                isConnected = false;
                updateEventListeners(ConnectionState.DISCONNECTED);
            } catch (IOException e) {
                Log.e("Err", "Could not close the connect socket", e);
            }
        }
    }

}
