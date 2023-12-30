package com.example.hypermile.bluetooth;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.hypermile.R;

import java.io.Serializable;

public class DiscoveredDevice {
    private String name;
    private String macAddress;
    private DeviceSelectedCallback deviceSelectedCallback;
    private BluetoothDevice bluetoothDevice;


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

    public View setViewContent(View view) {
        TextView deviceName = view.findViewById(R.id.deviceNameView);
        TextView deviceMac = view.findViewById(R.id.deviceMacView);

        view.findViewById(R.id.deviceItemLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deviceSelectedCallback.deviceSelected(DiscoveredDevice.this);
                }
            });

        deviceName.setText(getName());
        deviceMac.setText(getMacAddress());
        return view;
    }
}

























