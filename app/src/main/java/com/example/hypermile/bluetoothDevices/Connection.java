package com.example.hypermile.bluetoothDevices;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


// Singleton class since we only ever have one bluetooth device connected
public class Connection {
    private static Connection instance;
    private static ConnectionThread connectionThread;

    private static byte[] inputBuffer;
    private static int newDataLen = 0;

    public static Connection getInstance() {
        if (instance == null) {
            instance = new Connection();
        }
        return instance;
    }
    private Connection() {
        inputBuffer = new byte[1024];
    }

    public static boolean hasConnection() {
        return connectionThread != null;
    }

    public static boolean hasData() {
        return newDataLen > 0;
    }

    public static void readBuffer(ByteArrayOutputStream out) throws IOException {
        out.write(inputBuffer);
        out.write('\0');
        out.flush();
        out.close();
        newDataLen = 0;
    }

    public static void request(byte[] command) {
        if (hasConnection()) {
            connectionThread.write(command);
        }
    }

    public void createConnection(BluetoothDevice bluetoothDevice) {
        if (connectionThread != null) {
            connectionThread.cancel();
        }
        ConnectThread connectThread = new ConnectThread(bluetoothDevice);
        connectThread.start();
    }

    private void manageConnection(BluetoothSocket bluetoothSocket) {
        connectionThread = new ConnectionThread(bluetoothSocket);
        connectionThread.start();
    }

    class ConnectThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final BluetoothDevice bluetoothDevice;


        public ConnectThread(BluetoothDevice bluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice;
            BluetoothSocket _socket = null;

            try {
                _socket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            }
            catch (IOException | SecurityException e) {
                Log.d("Err", "ConnectThread: Failed to create socket");
            }
            bluetoothSocket = _socket;
        }


        public void run() {
            Log.d("ble", "running");
            // Cancel discovery because it otherwise slows down the connection.
//            try {
//
//            }
//            catch (SecurityException e) {
//                Log.e("Err", "run: Failed to cancel discovery", e);
//            }
            try {
                bluetoothSocket.connect();
            } catch (IOException | SecurityException connectException) {
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


    private class ConnectionThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

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
            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    newDataLen = inputStream.read(inputBuffer);

//                    String response = new String(inputBuffer, StandardCharsets.UTF_8);
//
//                    Log.d("Res", response);
                } catch (IOException e) {
                    Log.d("Err", "Input stream was disconnected", e);
                    break;
                }
            }
        }


        public void write(byte[] bytes) {
            try {
                Log.d("send", " try write: " + bytes);
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.e("Err", "Error occurred when sending data", e);
            }
        }


        public PipedOutputStream getOutputStream() throws IOException {
            return new PipedOutputStream((PipedInputStream) inputStream);
        }

        public PipedInputStream getInputStream() throws IOException {
            return new PipedInputStream((PipedOutputStream) outputStream);
        }


        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e("Err", "Could not close the connect socket", e);
            }
        }
    }

}
