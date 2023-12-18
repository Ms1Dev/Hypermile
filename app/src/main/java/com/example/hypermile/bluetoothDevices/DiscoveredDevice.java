package com.example.hypermile.bluetoothDevices;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
    private FrameLayout statusImageLayout;
    private DeviceSelectedCallback deviceSelectedCallback;
    ImageView connectButton;
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
        statusImageLayout = view.findViewById(R.id.statusImage);

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
                            deviceSelectedCallback.deviceSelected(DiscoveredDevice.this);
                            return true;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });

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
        catch (Exception _) {
            return false;
        }
    }

    @Override
    public void onStateChange(ConnectionState connectionState) {
        if (statusImageLayout == null) return;

        ((Activity) view.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (connectionState) {
                    case CONNECTED:
                        Log.d("f", "run: Tickk");
                        view.findViewById(R.id.device_connectedTick).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.device_connectingProgress).setVisibility(View.INVISIBLE);
                        break;
                    case CONNECTING:
                        view.findViewById(R.id.device_connectedTick).setVisibility(View.INVISIBLE);
                        view.findViewById(R.id.device_connectingProgress).setVisibility(View.VISIBLE);
                        break;
                    case DISCONNECTED:

                        break;
                }
            }
        });


    }
}

























