package com.example.hypermile.bluetoothDevices;


import android.bluetooth.BluetoothDevice;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.hypermile.MainActivity;
import com.example.hypermile.R;

public class DiscoveredDevice implements DiscoveredDeviceListElement {
    private String name;
    private String macAddress;
    ImageView connectButton;
    private BluetoothDevice bluetoothDevice;

    public DiscoveredDevice(BluetoothDevice bluetoothDevice, String name) {
        this.bluetoothDevice = bluetoothDevice;
        this.name = name;
        this.macAddress = bluetoothDevice.getAddress();
    }

    public String getName() {
        return name == null? "Unknown Device" : name;
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

                PopupMenu popupMenu = new PopupMenu(view.getContext(), connectButton);
                popupMenu.getMenuInflater().inflate(R.menu.bluetooth_device_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int item_id = menuItem.getItemId();

                        if (item_id == R.id.connectThisDevice) {
                            Connection.getInstance().createConnection(bluetoothDevice);
                            return true;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });

        deviceName.setText(getName());
        deviceMac.setText(getMacAddress());
        return view;
    }

    @Override
    public boolean cmp(DiscoveredDeviceListElement comparison) {
        try {
           DiscoveredDevice comparisonDevice = (DiscoveredDevice) comparison;
           return comparisonDevice.macAddress == this.macAddress;
        }
        catch (Exception _) {
            return false;
        }
    }
}

























