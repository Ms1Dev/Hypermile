package com.example.hypermile.bluetoothDevices;


import android.view.View;
import android.widget.TextView;

import com.example.hypermile.R;

public class DiscoveredDevice implements DiscoveredDeviceListElement {
    private String name;
    private String macAddress;

    public DiscoveredDevice(String name, String macAddress) {
        this.name = name;
        this.macAddress = macAddress;
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
        deviceName.setText(getName());
        deviceMac.setText(getMacAddress());
        return view;
    }
}

























