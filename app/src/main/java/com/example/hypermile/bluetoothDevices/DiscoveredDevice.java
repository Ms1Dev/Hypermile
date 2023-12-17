package com.example.hypermile.bluetoothDevices;


import android.bluetooth.BluetoothDevice;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.hypermile.R;

public class DiscoveredDevice implements DiscoveredDeviceListElement {
    private String name;
    private String macAddress;
    Button connectButton;
    private BluetoothDevice bluetoothDevice;

    public DiscoveredDevice(BluetoothDevice bluetoothDevice, String name) {
        this.bluetoothDevice = bluetoothDevice;
        this.name = name;
        this.macAddress = bluetoothDevice.getAddress();
    }

    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public int getResource() {
        return R.layout.discovered_device_list_item;
    }

    @Override
    public View setViewContent(View view) {
        TextView deviceName = view.findViewById(R.id.deviceNameView);
        TextView deviceMac = view.findViewById(R.id.deviceMacView);

        connectButton = view.findViewById(R.id.connectButton);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Connection connection = Connection.getInstance();
                connection.createConnection(bluetoothDevice);
            }
        });

        deviceName.setText(getName());
        deviceMac.setText(getMacAddress());
        return view;
    }
}

























