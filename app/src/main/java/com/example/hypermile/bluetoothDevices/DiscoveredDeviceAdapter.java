package com.example.hypermile.bluetoothDevices;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.hypermile.R;

import java.util.List;

// SOURCE: lab 5

public class DiscoveredDeviceAdapter extends ArrayAdapter {

    public DiscoveredDeviceAdapter(Context context, List<DiscoveredDevice> deviceList) {
        super(context, 0, deviceList);
    }

    public View getView(int position, @NonNull View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.discovered_device_list_item, parent, false);
        }

        DiscoveredDevice device = (DiscoveredDevice) getItem(position);
        TextView deviceName = convertView.findViewById(R.id.deviceNameView);
        TextView deviceMac = convertView.findViewById(R.id.deviceMacView);

        if (device != null) {
            deviceName.setText(device.getName());
            deviceMac.setText(device.getMacAddress());
        }

        return convertView;
    }
}
