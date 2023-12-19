package com.example.hypermile.bluetoothDevices;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.hypermile.R;

public class DiscoveredDeviceSectionHeader implements DiscoveredDeviceListElement {
    String title;
    ProgressBar progressBar;
    boolean showProgressBar = false;
    public DiscoveredDeviceSectionHeader(String title) { this.title = title; }

    @Override
    public int getResource() {
        return R.layout.discovered_device_section_header;
    }

    public void showHideProgressBar(boolean show) {
        showProgressBar = show;
        if (progressBar != null) {
            progressBar.setVisibility(show? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public View setViewContent(View view) {
        TextView headerView = view.findViewById(R.id.sectionHeaderView);
        headerView.setText(title);
        progressBar = view.findViewById(R.id.sectionHeaderProgressBar);
        showHideProgressBar(showProgressBar);
        return view;
    }

    @Override
    public boolean cmp(DiscoveredDeviceListElement comparison) {
        return false;
    }
}
