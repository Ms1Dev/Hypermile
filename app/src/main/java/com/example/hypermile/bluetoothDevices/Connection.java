package com.example.hypermile.bluetoothDevices;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import com.example.hypermile.obd.ObdFrame;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;


// Singleton class since we only ever have one bluetooth device
public class Connection {

    private static final int CONNECTION_ATTEMPTS = 3;
    private static final int MONITOR_FREQUENCY = 5000;

    private static final String PREFERENCE_FILENAME = "Hypermile_preferences";
    private static final String PREFERENCE_DEVICE_MAC = "ConnectedDeviceMAC";

    private final static int INPUT_BUFFER_SIZE = 1024;
    private final static int SEARCH_FOR_PROTOCOL_TIMEOUT = 20000;
    private final static int SEARCH_FOR_PROTOCOL_ATTEMPTS = 3;
    private static Connection instance;
    private ConnectionThread connectionThread;
    private InitConnThread initConnThread;
    private BluetoothDevice bluetoothDevice;
    private ObdFrame latestFrame;
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private boolean isConnected = false;
    private int autoConnectAttempts = 0;
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

    public static Connection getInstance() {
        if (instance == null) {
            instance = new Connection();
            instance.autoConnect();
        }
        return instance;
    }

    public void autoConnectFailed() {
        updateEventListeners(ConnectionState.BLUETOOTH_FAIL);
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


    public ObdFrame getLatestFrame() {
        ObdFrame obdFrame = latestFrame;
        latestFrame = null;
        return obdFrame;
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
        if (bluetoothDevice == null) return;
        this.bluetoothDevice = bluetoothDevice;
        autoConnectAttempts = 0;

        if (connectionThread != null) {
            connectionThread.cancel();
        }

        if (initConnThread != null) {
            initConnThread.cancel();
        }

        updateEventListeners(ConnectionState.BLUETOOTH_CONNECTING);

        initConnThread = new InitConnThread(bluetoothDevice);
        initConnThread.start();
    }


    /**
     * Spawns a ConnectionThread which manages communication with the device
     * @param bluetoothSocket
     */
    private void manageConnection(BluetoothSocket bluetoothSocket) {
        connectionThread = new ConnectionThread(bluetoothSocket);
        connectionThread.start();

        updateEventListeners(ConnectionState.OBD_CONNECTING);

        updateEventListeners(initialise()? ConnectionState.CONNECTED : ConnectionState.OBD_FAIL);

        isConnected = true;
    }


    private void autoConnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(MONITOR_FREQUENCY);
                        ConnectionState connectionState = getConnectionState();
                        if (connectionState == ConnectionState.DISCONNECTED && autoConnectAttempts < CONNECTION_ATTEMPTS &&  bluetoothDevice != null)
                        {
                            autoConnectAttempts++;
                            createConnection(bluetoothDevice);
                        }
                        else if (connectionState == ConnectionState.CONNECTED) {
                            autoConnectAttempts = 0;
                        }
                        else if (autoConnectAttempts >= CONNECTION_ATTEMPTS) {
                            autoConnectFailed();
                        }
                    } catch (InterruptedException e) {
                        //TODO:
                    }
                }
            }
        }).start();
    }


    public boolean initialise() {
        // elm327 documentation
        // https://www.elmelectronics.com/DSheets/ELM327DSH.pdf

        try {
            sendCommand("ATD\r"); // set all defaults
            sendCommand("ATZ\r"); // reset
            sendCommand("ATE0\r"); // echo command off
            sendCommand("ATL1\r"); // line feeds on
            sendCommand("ATS1\r"); // spaces between bytes on
            sendCommand("ATH1\r"); // headers on

            return findProtocol();

        } catch (InterruptedException | IOException e) {
            return false;
        }
    }


    private boolean sendCommand(String command) throws IOException, InterruptedException {
        byte[] cmdAsBytes = command.getBytes();
        if (connectionThread != null) {
            connectionThread.write(cmdAsBytes);
            Thread.sleep(200);
            return true;
        }
        return false;
    }


    private boolean findProtocol() throws IOException, InterruptedException {

        int attempts = 0;

        do {
            long startMillis = System.currentTimeMillis();
            attempts++;

            sendCommand("ATSP0\r"); // Auto detect protocol
            sendCommand("0100\r"); // Request available PIDs (just to test for response)

            while (!hasData()) {
                Thread.sleep(100);
                if(startMillis + SEARCH_FOR_PROTOCOL_TIMEOUT < System.currentTimeMillis()) {
                   break;
                }
            }
            ObdFrame obdFrame = getLatestFrame();
            if (obdFrame != null && obdFrame.isReponse()) return true;

        } while (attempts < SEARCH_FOR_PROTOCOL_ATTEMPTS);
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
                //TODO: figure out why this UUID is used
                _socket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            }
            catch (IOException | SecurityException e) {
                Log.d("Err", "ConnectThread: Failed to create socket");
            }
            bluetoothSocket = _socket;
        }


        public void run() {
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

        public void cancel() {
            try {
                bluetoothSocket.close();
                isConnected = false;
            } catch (IOException closeException) {
                Log.e("Err", "Could not close the client socket", closeException);
            }
            this.isInterrupted();
        }
    }

    /**
     * Manages the connection and communication with the device.
     */
    private class ConnectionThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        Connection connection;

        public ConnectionThread(BluetoothSocket socket) {
            bluetoothSocket = socket;
            connection = Connection.getInstance();
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
            while (true) {
                try {
                    byte[] buffer = new byte[INPUT_BUFFER_SIZE];
                    int bytesIn = inputStream.read(buffer);

                    if (bytesIn > 0) {
                        ObdFrame obdFrame = ObdFrame.createFrame(buffer);
                        if (obdFrame != null) latestFrame = obdFrame;
                    }

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
                isConnected = false;
                updateEventListeners(ConnectionState.DISCONNECTED);
            } catch (IOException e) {
                Log.e("Err", "Could not close the connect socket", e);
            }
        }
    }

}
