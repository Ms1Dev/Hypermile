package com.example.hypermile.bluetoothDevices;

import android.view.View;
import android.widget.TextView;

import com.example.hypermile.R;

public class DiscoveredDeviceSectionHeader implements DiscoveredDeviceListElement {
    String title;
    public DiscoveredDeviceSectionHeader(String title) { this.title = title; }

    @Override
    public int getResource() {
        return R.layout.discovered_device_section_header;
    }

    @Override
    public View setViewContent(View view) {
        TextView headerView = view.findViewById(R.id.sectionHeaderView);
        headerView.setText(title);
        return view;
    }

    @Override
    public boolean cmp(DiscoveredDeviceListElement comparison) {
        return false;
    }
}