package com.example.hypermile.bluetooth;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.hypermile.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Array adapter used to show a list of discovered devices.
 * SOURCE: lab 5
 */
public class DiscoveredDeviceAdapter extends ArrayAdapter<DiscoveredDevice> {

    List<DiscoveredDevice> deviceList;

    public DiscoveredDeviceAdapter(Context context, List<DiscoveredDevice> deviceList) {
        super(context, 0, deviceList);
        this.deviceList = deviceList;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        DiscoveredDevice element = (DiscoveredDevice) getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.discovered_device_list_item, parent, false);
        }

        try {
            element.setViewContent(convertView);
        }
        catch (NullPointerException e) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.discovered_device_list_item, parent, false);
            element.setViewContent(convertView);
        }

        return convertView;
    }

    @Override
    public void add(@Nullable DiscoveredDevice device) {
        for (DiscoveredDevice discoveredDevice : deviceList) {
            if (discoveredDevice.getMacAddress().equals(((DiscoveredDevice) device).getMacAddress())) {
                return;
            }
        }
        super.add(device);
    }
}
