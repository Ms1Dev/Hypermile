package com.example.hypermile.bluetoothDevices;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.hypermile.R;

import java.util.ArrayList;
import java.util.List;

// SOURCE: lab 5

public class DiscoveredDeviceAdapter extends ArrayAdapter {

    List<DiscoveredDeviceListElement> deviceList = new ArrayList<>();

    public DiscoveredDeviceAdapter(Context context, List<DiscoveredDeviceListElement> deviceList) {
        super(context, 0, deviceList);
        this.deviceList = deviceList;
    }

    public View getView(int position, @NonNull View convertView, ViewGroup parent) {
        DiscoveredDeviceListElement element = (DiscoveredDeviceListElement) getItem(position);

        if (element == null) {
            return convertView;
        }

        convertView = LayoutInflater.from(getContext()).inflate(element.getResource(), parent, false);

        return element.setViewContent(convertView);
    }

    @Override
    public void add(@Nullable Object object) {
        for (DiscoveredDeviceListElement element : deviceList) {
            if (((DiscoveredDeviceListElement) object).cmp(element)) {
                return;
            }
        }
        super.add(object);
    }
}
