package com.example.hypermile.bluetooth;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.hypermile.R;

public class DiscoveredDevice implements DiscoveredDeviceListElement, ConnectionEventListener {
    boolean isSelected = false;
    private String name;
    private String macAddress;
    private View view;
    private DeviceSelectedCallback deviceSelectedCallback;
    ImageView connectButton;
    private BluetoothDevice bluetoothDevice;
    private ProgressBar progressBar;
    private ImageView connectedTick;
    private ImageView noConnection;

    public DiscoveredDevice(BluetoothDevice bluetoothDevice, String name, DeviceSelectedCallback deviceSelectedCallback) {
        this.deviceSelectedCallback = deviceSelectedCallback;
        this.bluetoothDevice = bluetoothDevice;
        this.name = name;
        macAddress = bluetoothDevice.getAddress();
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }
    public String getName() {
        return name == null? "Unknown Device" : name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        if (isSelected) {
            Connection.getInstance().addConnectionEventListener(this);
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }
        else {
            Connection.getInstance().removeConnectionEventListener(this);
            noConnection.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            connectedTick.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getResource() {
        return R.layout.discovered_device_list_item;
    }

    @Override
    public View setViewContent(View view) {
        this.view = view;
        TextView deviceName = view.findViewById(R.id.deviceNameView);
        TextView deviceMac = view.findViewById(R.id.deviceMacView);

        view.findViewById(R.id.deviceItemLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deviceSelectedCallback.deviceSelected(DiscoveredDevice.this);
                }
            });

        progressBar = view.findViewById(R.id.device_connectingProgress);
        connectedTick = view.findViewById(R.id.device_connectedTick);
        noConnection = view.findViewById(R.id.device_noConnection);
        progressBar.setVisibility(View.INVISIBLE);

        if (isSelected) {
            onStateChange(Connection.getInstance().getConnectionState());
        }

        deviceName.setText(getName());
        deviceMac.setText(getMacAddress());
        return view;
    }

    @Override
    public boolean cmp(DiscoveredDeviceListElement comparison) {
        try {
           DiscoveredDevice comparisonDevice = (DiscoveredDevice) comparison;
           return comparisonDevice.macAddress.equals(this.macAddress);
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onStateChange(ConnectionState connectionState) {
        if (connectedTick == null || progressBar == null || noConnection == null) return;
        ((Activity) view.getContext()).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                switch (connectionState) {
                    case CONNECTED:
                        connectedTick.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        noConnection.setVisibility(View.INVISIBLE);

                        break;
                    case BLUETOOTH_CONNECTING:
                    case OBD_CONNECTING:
                        progressBar.setVisibility(View.VISIBLE);
                        connectedTick.setVisibility(View.INVISIBLE);
                        noConnection.setVisibility(View.INVISIBLE);

                        break;
                    case BLUETOOTH_FAIL:
                    case OBD_FAIL:
                        noConnection.setVisibility(View.VISIBLE);
                        connectedTick.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        break;

                }
            }
        });


    }
}

























