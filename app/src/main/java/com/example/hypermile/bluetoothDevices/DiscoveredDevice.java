package com.example.hypermile.bluetoothDevices;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.hypermile.MainActivity;
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
        }
        else {
            Connection.getInstance().removeConnectionEventListener(this);
            view.findViewById(R.id.device_connectedTick).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.device_connectingProgress).setVisibility(View.INVISIBLE);
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
        if (connectedTick == null || progressBar == null) return;
        ((Activity) view.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (connectionState) {
                    case CONNECTED:
                        connectedTick.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        break;
                    case CONNECTING:
                        connectedTick.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case DISCONNECTED:

                        break;
                }
            }
        });


    }
}

























